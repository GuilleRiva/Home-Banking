package com.home_banking_.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.home_banking_.dto.request.LoanGrantRequestDto;
import com.home_banking_.dto.request.LoanSimulationRequestDto;
import com.home_banking_.dto.response.LoanResponseDto;
import com.home_banking_.enums.IdempotencyOperation;
import com.home_banking_.enums.LoanStatus;
import com.home_banking_.enums.StatusAccount;
import com.home_banking_.enums.audit.AuditType;
import com.home_banking_.enums.audit.LoanAuditAction;
import com.home_banking_.exceptions.custom.AccountStateException;
import com.home_banking_.exceptions.custom.BusinessException;
import com.home_banking_.exceptions.custom.ResourceNotFoundException;
import com.home_banking_.mappers.LoanMapper;
import com.home_banking_.model.Account;
import com.home_banking_.model.Loan;
import com.home_banking_.repository.AccountRepository;
import com.home_banking_.repository.LoanRepository;
import com.home_banking_.service.AuditLogService;
import com.home_banking_.service.LoanService;
import com.home_banking_.service.idempotency.IdempotencyService;
import com.home_banking_.service.idempotency.IdempotencyValidationResult;
import com.home_banking_.service.security.CurrentUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

@Slf4j
@Service
public class LoanServiceImpl implements LoanService {

    private final AccountRepository accountRepository;
    private final LoanRepository loanRepository;
    private final LoanMapper loanMapper;
    private final CurrentUserService currentUserService;
    private final AuditLogService auditLogService;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    private static final BigDecimal DEFAULT_INTEREST_RATE = new BigDecimal("0.20");
    private static final BigDecimal MIN_LOAN_AMOUNT = new BigDecimal("1000");
    private static final BigDecimal MAX_LOAN_AMOUNT = new BigDecimal("1000000");
    private static final int MIN_INSTALLMENTS = 1;
    private static final int MAX_INSTALLMENTS = 60;

    public LoanServiceImpl(AccountRepository accountRepository, LoanRepository loanRepository, LoanMapper loanMapper, CurrentUserService currentUserService, AuditLogService auditLogService, IdempotencyService idempotencyService, ObjectMapper objectMapper) {
        this.accountRepository = accountRepository;
        this.loanRepository = loanRepository;
        this.loanMapper = loanMapper;
        this.currentUserService = currentUserService;
        this.auditLogService = auditLogService;
        this.idempotencyService = idempotencyService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    @Override
    public LoanResponseDto simulateLoans(LoanSimulationRequestDto dto) {
        String email = currentUserService.getCurrentUserEmail();
        Long userId = currentUserService.getCurrentUserId();

        log.info("[LOAN_SIMULATION_INIT] userEmail={} accountId={} amount={} installments={}",
                email, dto.getAccountId(), dto.getAmount(), dto.getInstallments());

        BigDecimal amount = dto.getAmount();
        validateAmount(amount);
        validateLoanRequest(dto.getAmount(), dto.getInstallments());

        Account account = getOwnedAccount(dto.getAccountId(), email);

        validateAndTraceActiveAccount(account, "LOAN_SIMULATION","LOAN_SIMULATION_REJECTED", userId);

        Loan simulatedLoan = buildLoanFromDto(dto.getAmount(), dto.getInstallments(), account);

        log.info("[LOAN_SIMULATION_SUCCESS] userEmail={} accountId={} amount={} installments={} totalToPay={}",
                email, dto.getAccountId(), dto.getAmount(), dto.getInstallments(), simulatedLoan.getTotalToPay());

        auditLogService.registerEvent(
                userId,
                "Loan simulation completed successfully. accountId= " + account.getId()
                        + ", amount= " + dto.getAmount()
                + ", installments= " + dto.getInstallments()
                + ", totalToPay= " + simulatedLoan.getTotalToPay(),
                LoanAuditAction.LOAN_SIMULATION_COMPLETED,
                AuditType.LOAN
        );
        return loanMapper.toDto(simulatedLoan);
    }

    @Transactional
    @Override
    public LoanResponseDto grantLoan(String idempotencyKey, LoanGrantRequestDto dto) {
        Long userId = currentUserService.getCurrentUserId();

        log.info("[LOAN_GRANT_IDEMPOTENCY_INIT] userId={} accountId={} amount={} installments={}",
                userId, dto.getAccountId(), dto.getAmount(), dto.getInstallments());

        IdempotencyValidationResult result = idempotencyService.validateAndRegister(
                idempotencyKey,
                userId,
                IdempotencyOperation.LOAN_GRANT,
                dto
        );

        if (result.isReplay()) {
            log.info("[LOAN_GRANT_IDEMPOTENCY_REPLAY] userId={} accountId={}",
                    userId, dto.getAccountId());

            return deserializeLoanResponse(result.getRecord().getResponseBody());
        }

        if (result.isProcessing()) {
            log.warn("[LOAN_GRANT_IDEMPOTENCY_PROCESSING] userId={} accountId={} recordId={}",
                    userId, dto.getAccountId(), result.getRecord().getId());

            throw new BusinessException("This loan request is currently being processed");
        }

        return idempotencyService.executeAndComplete(
                result.getRecord(),
                ()-> executeGrantLoan(dto),
                LoanResponseDto::getId,
                HttpStatus.CREATED,
                "Unexpected error during loan grant"
        );
    }


    private LoanResponseDto executeGrantLoan(LoanGrantRequestDto dto) {
        String email = currentUserService.getCurrentUserEmail();
        Long userId = currentUserService.getCurrentUserId();

        log.info("[LOAN_GRANT_INIT] userEmail={} accountId={} amount={} installments={}",
                email, dto.getAccountId(), dto.getAmount(), dto.getInstallments());

        validateLoanGrantRequest(dto);

        Account account = getOwnedAccount(dto.getAccountId(), email);

        validateAndTraceActiveAccount(account,"LOAN_GRANT", "LOAN_REJECTED", userId);

        validateAccountCanReceiveLoan(account, userId);

        Loan loan = buildGrantedLoan(dto,account);

        account.setBalance(account.getBalance().add(dto.getAmount()));

        accountRepository.save(account);
        Loan savedLoan = loanRepository.save(loan);

        auditLogService.registerEvent(
                userId,
                "Loan granted successfully. loanId= " + savedLoan.getId()
                        + ", accountId= " + account.getId()
                        + ", installments= " + savedLoan.getInstallments(),
                LoanAuditAction.LOAN_GRANTED,
                AuditType.LOAN
        );

        log.info("[LOAN_GRANT_SUCCESS] loanId={} accountId={} amount={} currency={} installments={} totalToPay={} endDate={}",
                loan.getId(),
                account.getId(),
                loan.getAmount(),
                loan.getCurrency(),
                loan.getInstallments(),
                loan.getTotalToPay(),
                loan.getEndDate());

        return loanMapper.toDto(savedLoan);
    }


    @Transactional
    @Override
    public LoanResponseDto requestLoan(String idempotencyKey, LoanGrantRequestDto dto)  {
        Long userId = currentUserService.getCurrentUserId();

        IdempotencyValidationResult result = idempotencyService.validateAndRegister(
                idempotencyKey,
                userId,
                IdempotencyOperation.REQUEST_LOAN,
                dto
        );

        if (result.isReplay()) {
            return deserializeLoanResponse(result.getRecord().getResponseBody());
        }

        if (result.isProcessing()) {
            throw new BusinessException("This loan request is currently being processed");
        }

        return idempotencyService.executeAndComplete(
                result.getRecord(),
                ()-> executeRequestLoan(dto),
                LoanResponseDto::getId,
                HttpStatus.CREATED,
                "Unexpected error during request loan"
        );
    }


    private LoanResponseDto executeRequestLoan (LoanGrantRequestDto dto) {
        String email = currentUserService.getCurrentUserEmail();
        Long userId = currentUserService.getCurrentUserId();

        log.info("[LOAN_REQUEST_INIT] userEmail={} accountId={} amount={} installments={}",
                email, dto.getAccountId(), dto.getAmount(), dto.getInstallments());

        validateLoanGrantRequest(dto);

        Account account = getOwnedAccountForUpdate(dto.getAccountId(), email);

        validateAndTraceActiveAccount(account, "LOAN_REQUEST", "LOAN_REJECTED", userId);

        validateAccountCanReceiveLoan(account,userId);

        LocalDateTime now = LocalDateTime.now();

        Loan loan = buildGrantedLoan(dto,account);

        Loan savedLoan = loanRepository.save(loan);

        auditLogService.registerEvent(
                userId,
                "Loan request created successfully. loanId= " + savedLoan.getId()
                + ", accountId= " + account.getId()
                + ", installments= " + savedLoan.getInstallments()
                + ", currency= " + savedLoan.getCurrency(),
                LoanAuditAction.LOAN_REQUEST_CREATED,
                AuditType.LOAN
        );

        log.info("[LOAN_REQUEST_SUCCESS] userId={} loanId={} accountId={} amount={} currency={} installments={} status={}",
                userId,
                savedLoan.getId(),
                account.getId(),
                savedLoan.getAccount(),
                savedLoan.getCurrency(),
                savedLoan.getInstallments(),
                savedLoan.getLoanStatus());

        return loanMapper.toDto(savedLoan);
    }


    @Transactional(readOnly = true)
    @Override
    public Optional<LoanResponseDto> getLoanByAccount(Long accountId) {
        String email = currentUserService.getCurrentUserEmail();

        log.info("[FETCH_LOAN_INIT] userEmail={} accountId={}",email, accountId);

        getOwnedAccount(accountId, email);

        Optional<Loan> loan = loanRepository.findByAccountId(accountId);

        if (loan.isPresent()){
            log.info("[FETCH_LOAN_SUCCESS] userEmail={} accountId={} loanId={} totalToPay={}",
                    email, accountId, loan.get().getId(), loan.get().getTotalToPay());
        }else {
            log.warn("[FETCH_LOAN_NOT_FOUND] userEmail={} accountId={}",email, accountId);
        }
        return loan.map(loanMapper::toDto);
    }


    private Loan buildLoanFromDto(BigDecimal amount, Integer installments, Account account) {
        BigDecimal interestRate = DEFAULT_INTEREST_RATE;
        BigDecimal totalToPay = amount.add(amount.multiply(interestRate));
        BigDecimal installmentAmount = totalToPay.divide(BigDecimal.valueOf(installments), 2, RoundingMode.HALF_UP);

        Loan loan = new Loan();
        loan.setAccount(account);
        loan.setAmount(amount);
        loan.setInstallments(installments);
        loan.setInterestRate(interestRate);
        loan.setTotalToPay(totalToPay);
        loan.setInstallmentsAmount(installmentAmount);
        return loan;
    }

    private Loan buildGrantedLoan(LoanGrantRequestDto dto, Account account) {
        Loan loan = buildLoanFromDto(dto.getAmount(),dto.getInstallments(), account);
        LocalDateTime now = LocalDateTime.now();

        loan.setLoanStatus(LoanStatus.ACTIVE);
        loan.setStartDate(now);
        loan.setEndDate(now.plusMonths(dto.getInstallments()));
        loan.setCurrency(account.getCurrency());

        return loan;
    }

    @Override
    public boolean existsByAccountIdAndStatusLoan(Long accountId, LoanStatus statusLoan) {
        return false;
    }


    private void validateLoanRequest(BigDecimal amount, Integer installments) {

        if (amount== null || amount.compareTo(BigDecimal.ZERO) <=0) {
            throw new BusinessException("Loan amount must be greater than zero");
        }
        if (amount.compareTo(MIN_LOAN_AMOUNT) < 0) {
            throw new BusinessException("Loan amount is below the minimum allowed");
        }
        if (amount.compareTo(MAX_LOAN_AMOUNT) > 0) {
            throw new BusinessException("Loan amount exceeds the maximum allowed");
        }
        if (installments < MIN_INSTALLMENTS || installments > MAX_INSTALLMENTS) {
            throw new BusinessException("Invalid number of installments");
        }
    }


    private void validateLoanGrantRequest(LoanGrantRequestDto dto) {
        validateAmount(dto.getAmount());
        validateLoanRequest(dto.getAmount(), dto.getInstallments());
    }


    private void validateAccountCanReceiveLoan(Account account, Long userId){
        validateAccountHasNoActiveLoan(account,userId);
        validateAccountHasNoPendingLoan(account,userId);
    }


    private void validateAndTraceActiveAccount(Account account, String logPrefix, String eventType, Long userId) {
        if (account.getStatusAccount() != StatusAccount.ACTIVE) {
            log.warn("[{}_REJECTED] Account is not active. userId={} accountId={} status={}",
                    logPrefix, userId, account.getId(), account.getStatusAccount());

            auditLogService.registerEvent(
                    userId,
                    logPrefix + " rejected because account is not active. accountId= "
                            + account.getId() + ", status= " + account.getStatusAccount(),
                    LoanAuditAction.LOAN_REJECTED,
                    AuditType.LOAN
            );

            throw new AccountStateException(
                    "Account " + account.getId() + " is not active and cannot perform operations"
            );
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Amount must be positive");
        }
        if (amount.scale() > 2) {
            throw new BusinessException("Loan amount must have at most 2 decimal places");
        }
    }

    private Account getOwnedAccount(Long accountId, String email) {
        return accountRepository.findByIdAndUsersEmail(accountId, email)
                .orElseThrow(()-> new ResourceNotFoundException("Account not found"));
    }

    private Account getOwnedAccountForUpdate(Long accountId, String email) {
        return accountRepository.findByIdAndUsersEmailForUpdate(accountId, email)
                .orElseThrow(()-> new ResourceNotFoundException("Account not found"));
    }

    private void validateAccountHasNoPendingLoan(Account account, Long userId) {

        boolean existsPendingLoan = loanRepository.existsByAccountIdAndLoanStatusIn(
                account.getId(),
                Collections.singletonList(LoanStatus.PENDING)
        );

        if (existsPendingLoan) {
            log.warn("[LOAN_REQUEST_REJECTED_PENDING_LOAN_EXISTS] userId={} accountId={}",
                    userId, account.getId());

            auditLogService.registerEvent(
                    userId,
                    "Loan request rejected because account already has a pending loan. accountId= " + account.getId(),
                    LoanAuditAction.LOAN_REJECTED,
                    AuditType.LOAN
            );

            throw new AccountStateException("Account already has a pending loan request");
        }
    }

    private void validateAccountHasNoActiveLoan(Account account, Long userId) {
        boolean existsActiveLoan = loanRepository.existsByAccountIdAndLoanStatusIn(
                account.getId(),
                Collections.singletonList(LoanStatus.ACTIVE)
        );

        if (existsActiveLoan) {
            log.warn("[LOAN_REJECTED_ACTIVE_LOAN_EXISTS] userId={} accountId={}",
                    userId, account.getId());

            auditLogService.registerEvent(
                    userId,
                    "Loan rejected because account already has an active loan. accountId= " + account.getId(),
                    LoanAuditAction.LOAN_REJECTED,
                    AuditType.LOAN
            );

            throw new AccountStateException("Account already has an active loan");
        }
    }


    private LoanResponseDto deserializeLoanResponse(String responseBody) {
        try {
            return objectMapper.readValue(responseBody, LoanResponseDto.class);
        } catch (JsonProcessingException e) {
            log.error("[LOAN_GRANT_IDEMPOTENCY_DESERIALIZATION_ERROR]", e);

            throw new BusinessException("Could not recover previous loan response");
        }
    }
}
