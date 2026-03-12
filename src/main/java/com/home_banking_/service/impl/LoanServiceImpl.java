package com.home_banking_.service.impl;

import com.home_banking_.dto.request.LoanGrantRequestDto;
import com.home_banking_.dto.request.LoanSimulationRequestDto;
import com.home_banking_.dto.response.LoanResponseDto;
import com.home_banking_.enums.LoanStatus;
import com.home_banking_.enums.StatusAccount;
import com.home_banking_.exceptions.BusinessException;
import com.home_banking_.exceptions.ResourceNotFoundException;
import com.home_banking_.mappers.LoanMapper;
import com.home_banking_.model.Account;
import com.home_banking_.model.Loan;
import com.home_banking_.repository.AccountRepository;
import com.home_banking_.repository.LoanRepository;
import com.home_banking_.service.AuditLogService;
import com.home_banking_.service.LoanService;
import com.home_banking_.service.security.CurrentUserService;
import com.home_banking_.service.security.CurrentUserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class LoanServiceImpl implements LoanService {

    private final AccountRepository accountRepository;
    private final LoanRepository loanRepository;
    private final LoanMapper loanMapper;
    private final CurrentUserService currentUserService;
    private final AuditLogService auditLogService;

    private static final BigDecimal DEFAULT_INTEREST_RATE = new BigDecimal("0.20");
    private static final BigDecimal MIN_LOAN_AMOUNT = new BigDecimal("1000");
    private static final BigDecimal MAX_LOAN_AMOUNT = new BigDecimal("1000000");
    private static final int MIN_INSTALLMENTS = 1;
    private static final int MAX_INSTALLMENTS = 60;

    public LoanServiceImpl(AccountRepository accountRepository, LoanRepository loanRepository, LoanMapper loanMapper, CurrentUserServiceImpl currentUserService, AuditLogService auditLogService) {
        this.accountRepository = accountRepository;
        this.loanRepository = loanRepository;
        this.loanMapper = loanMapper;
        this.currentUserService = currentUserService;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    @Override
    public LoanResponseDto simulateLoans(LoanSimulationRequestDto dto) {
        String email = currentUserService.getCurrentUserEmail();

        log.info("[LOAN_SIMULATION_INIT] userEmail={} accountId={} amount={} installments={}",
                email, dto.getAccountId(), dto.getAmount(), dto.getInstallments());

        BigDecimal amount = dto.getAmount();
        validateAmount(amount);

        Account account = getOwnedAccount(dto.getAccountId(), email);

        log.warn("[LOAN_SIMULATION_REJECTED] Account not found or access denied. userEmail={} accountId={}",
        email, dto.getAccountId());

        validateAccountActive(account);
        validateLoanRequest(dto.getAmount(), dto.getInstallments());

        Loan simulatedLoan = buildLoanFromDto(dto.getAmount(), dto.getInstallments(), account);

        log.info("[LOAN_SIMULATED_SUCCESS] userEmail={} accountId={} amount={} installments={} totalToPay={}",
                email, dto.getAccountId(), dto.getAmount(), dto.getInstallments(), simulatedLoan.getTotalToPay());

        return loanMapper.toDto(simulatedLoan);
    }

    @Transactional
    public LoanResponseDto grantLoan(LoanGrantRequestDto dto) {
        String email = currentUserService.getCurrentUserEmail();
        Long userId = currentUserService.getCurrentUserId();

        log.info("[LOAN_GRANT_INIT] userEmail={} accountId={} amount={} installments={}",
                email, dto.getAccountId(), dto.getAmount(), dto.getInstallments());

        BigDecimal amount = dto.getAmount();
        validateAmount(amount);

        Account account = getOwnedAccountForUpdate(dto.getAccountId(), email);

        log.warn("[LOAN_GRANT_REJECTED] account not found or access denied. userEmail={} accountId={}",
                email, dto.getAccountId());

        validateAccountActive(account);
        validateLoanRequest(dto.getAmount(), dto.getInstallments());

        Optional<Loan> existingLoan = loanRepository.findByAccountId(account.getId());

        if (existingLoan.isPresent() && existingLoan.get().getStatusLoan() == LoanStatus.ACTIVE) {
            log.warn("[LOAN_GRANT_REJECTED] Account already has an active loan. accountId={}",
                    account.getId());
            throw new BusinessException("Account already has an active loan");
        }

        auditLogService.registerEvent(
                userId,
                "Loan rejected because account already has an active loan. accountId={}" + dto.getAccountId(),
                "LOAN_REJECTED",
                "SECURITY"
        );

        Loan loan = buildLoanFromDto(dto.getAmount(), dto.getInstallments(), account);
        loan.setStatusLoan(LoanStatus.ACTIVE);
        loan.setStartDate(LocalDateTime.now());
        loan.setEndDate(LocalDateTime.now().plusMonths(dto.getInstallments()));
        loan.setCurrency(account.getCurrency());

        account.setBalance(account.getBalance().add(dto.getAmount()));

        accountRepository.save(account);
        loanRepository.save(loan);

        auditLogService.registerEvent(
                userId,
                "Loan granted successfully. loanId= " + loan.getId()
                + ", accountId= " + account.getId()
                + ", amount= " + loan.getAmount()
                + ", installments= " + loan.getInstallments()
                + ", totalToPay=" + loan.getTotalToPay(),
                "LOAN_GRANTED",
                "TRANSACTION"
        );

        log.info("[LOAN_GRANT_SUCCESS] loanId={} accountId={} amount={} currency={} installments={} totalToPay={} endDate={}]",
                loan.getId(),
                account.getId(),
                loan.getAmount(),
                loan.getCurrency(),
                loan.getInstallments(),
                loan.getTotalToPay(),
                loan.getEndDate());

        return loanMapper.toDto(loan);
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

    @Transactional(readOnly = true)
    @Override
    public Optional<LoanResponseDto> getLoanByAccount(Long accountId) {
        String email = currentUserService.getCurrentUserEmail();

        log.info("[FETCH_LOAN_INIT] userEmail={} accountId={}",email, accountId);

        Account account = getOwnedAccount(accountId, email);

        Optional<Loan> loan = loanRepository.findByAccountId(accountId);

        if (loan.isPresent()){
            log.info("[FETCH_LOAN_SUCCESS] userEmail={} accountId={} totalToPay={}",
                    email,accountId, loan.get().getTotalToPay());
        }else {
            log.warn("[FETCH_LOAN_NOT_FOUND] userEmail={} accountId={}",email, accountId);
        }
        return loan.map(loanMapper::toDto);
    }

    // Helpers //
    private void validateLoanRequest(BigDecimal amount, Integer installments) {

        if (amount== null || amount.compareTo(BigDecimal.ZERO) <=0) {
            throw new BusinessException("Loan amount must be greater than zero");
        }
        if (amount.compareTo(MIN_LOAN_AMOUNT) > 0) {
            throw new BusinessException("Loan amount is below the minimum allowed");
        }
        if (amount.compareTo(MAX_LOAN_AMOUNT) > 0) {
            throw new BusinessException("Loan amount exceeds the maximum allowed");
        }
        if (installments < MIN_INSTALLMENTS || installments > MIN_INSTALLMENTS) {
            throw new BusinessException("Invalid number of installments");
        }
    }

    private void validateAccountActive(Account account){
        if (account.getStatusAccount() != StatusAccount.ACTIVE){
            throw new BusinessException(
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
}
