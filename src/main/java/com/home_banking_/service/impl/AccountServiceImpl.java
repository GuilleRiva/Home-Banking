package com.home_banking_.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.home_banking_.dto.request.AccountCreateRequestDto;
import com.home_banking_.dto.response.AccountResponseDto;
import com.home_banking_.enums.*;
import com.home_banking_.enums.audit.AccountAuditAction;
import com.home_banking_.enums.audit.AuditType;
import com.home_banking_.exceptions.custom.*;
import com.home_banking_.mappers.AccountMapper;
import com.home_banking_.model.Account;
import com.home_banking_.model.IdempotencyRecord;
import com.home_banking_.model.Users;
import com.home_banking_.repository.AccountRepository;
import com.home_banking_.repository.LoanRepository;
import com.home_banking_.repository.UsersRepository;
import com.home_banking_.service.AccountService;
import com.home_banking_.service.AuditLogService;
import com.home_banking_.service.idempotency.IdempotencyService;
import com.home_banking_.service.idempotency.IdempotencyValidationResult;
import com.home_banking_.service.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UsersRepository usersRepository;
    private final AccountMapper accountMapper;
    private final AuditLogService auditLogService;
    private final CurrentUserService currentUserService;
    private final LoanRepository loanRepository;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    private static final int MAX_ACCOUNTS_PER_USER = 3;
    private static final int MIN_ALIAS_LENGTH = 6;
    private static final int MAX_ALIAS_LENGTH = 20;

    @Transactional
    @Override
    public AccountResponseDto createAccount(String idempotencyKey, AccountCreateRequestDto dto) {
        Long userId = currentUserService.getCurrentUserId();

        IdempotencyValidationResult validationResult =
                idempotencyService.validateAndRegister(
                        idempotencyKey,
                        userId,
                        IdempotencyOperation.CREATE_ACCOUNT,
                        dto
                );

        if (validationResult.isReplay()) {
            log.info("[CREATE_ACCOUNT_IDEMPOTENT_REPLAY] userId={} idempotencyKey={}", userId, idempotencyKey);
            return replayResponse(validationResult.getRecord());
        }

        AccountResponseDto responseDto = executeCreateAccount(dto);

        completeIdempotencyRecord(validationResult.getRecord(), responseDto);

        return responseDto;
    }


    private AccountResponseDto executeCreateAccount(AccountCreateRequestDto dto) {
        String email = currentUserService.getCurrentUserEmail();

        log.info("[ACCOUNT_CREATE_INIT] userEmail={} alias={} accountType={}",
                email, maskAlias(dto.getAlias()), dto.getTypeAccount());

        Users users = usersRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        log.debug("[ACCOUNT_CREATE_MAPPING] authenticatedUserId={} authenticatedEmail={}", users.getId(), users.getEmail());

        validateUserIsActive(users);

        String normalizedAlias = validateAndNormalizeAlias(dto.getAlias());

        validateAccountCreationRules(users.getId(), dto.getTypeAccount(), normalizedAlias);

        Account account = accountMapper.toEntity(dto);
        account.setAlias(normalizedAlias);
        account.setAccountNumber(generateUniqueAccountNumber());
        account.setCBU(generateUniqueCbu());
        account.setUsers(users);
        account.setBalance(BigDecimal.ZERO);
        account.setCreationDate(LocalDateTime.now());
        account.setStatusAccount(StatusAccount.ACTIVE);
        account.setCurrency(Currency.ARS);

        log.debug("[ACCOUNT_CREATE_MAPPING] beforeSave userIdInAccount={} alias={} typeAccount={} ",
                account.getUsers() != null ? account.getUsers().getId() : null,
                account.getMaskAlias(),
                account.getTypeAccount());

        Account savedAccount=accountRepository.save(account);

        log.debug("[ACCOUNT_CREATE_MAPPING] afterSave accountId={} userIdInAccount={}",
                savedAccount.getId(),
                savedAccount.getUsers() != null ? savedAccount.getUsers().getId() : null);

        log.info("[ACCOUNT_CREATE_SUCCESS] userEmail={} accountId={} alias={} accountType={}",
                email, savedAccount.getId(), savedAccount.getMaskAlias(), savedAccount.getTypeAccount());

        return accountMapper.toDto(savedAccount);

    }


    
    @Transactional
    @Override
    public void deleteAccount(Long id) {
        String email = currentUserService.getCurrentUserEmail();

        log.info("[ACCOUNT_CLOSE_INIT] userEmail={} accountId={}", email, id);

        Account account = getOwnedAccountForUpdate(id, email);

        if (account.getStatusAccount() == StatusAccount.CLOSED) {
            log.warn("[ACCOUNT_CLOSE_REJECTED] reason=already_closed. userEmail={} accountId={}",
                    email, account.getId());

            auditLogService.registerEvent(
                    account.getUsers().getId(),
                    "Account closure rejected because is greater tha zero. accountId= " + account.getId()
                            + ", balance=" + account.getBalance(),
                    AccountAuditAction.ACCOUNT_CLOSE_REJECTED,
                    AuditType.ACCOUNT
            );
            throw new AccountStateException("Account is already closed");
        }

        validateAccountCanBeClosed(account);

        account.setStatusAccount(StatusAccount.CLOSED);
        accountRepository.save(account);

        auditLogService.registerEvent(
                account.getUsers().getId(),
                "Account closed successfully. accountId= " + account.getId()
                        + ", alias= " + account.getMaskAlias()
                        + ", status= " + account.getStatusAccount(),
                AccountAuditAction.CLOSED_ACCOUNT,
                AuditType.ACCOUNT
        );

        log.info("[ACCOUNT_CLOSE_SUCCESS] userEmail={} accountId={} status={}",
                email, account.getId(), account.getStatusAccount());
    }


    @Override
    @Transactional(readOnly = true)
    public List<AccountResponseDto> getAll() {
        String adminEmail= currentUserService.getCurrentUserEmail();

        log.info("[ACCOUNT_FETCH_ALL_INIT] adminEmail={}", adminEmail);

        List<AccountResponseDto> accounts = accountRepository.findAll()
                .stream()
                .map(accountMapper::toDto)
                .toList();

        log.debug("[ACCOUNT_FETCH_ALL_SUCCESS] adminEmail={} accountSize={}",adminEmail, accounts.size());
        return accounts;
    }


    @Override
    @Transactional(readOnly = true)
    public AccountResponseDto getAccountByIdForAdmin(Long accountId) {

        String requesterEmail = currentUserService.getCurrentUserEmail();

        log.info("[ADMIN_ACCOUNT_FETCH_BY_ID_INIT] requesterEmail={} accountId={}",
                requesterEmail, accountId);

        Account account = getAccountByIdOrThrow(accountId);

        log.info("[ADMIN_ACCOUNT_FETCH_BY_ID_SUCCESS] requesterEmail={} accountId={} status={}",
                requesterEmail,
                account.getId(),
                account.getStatusAccount());

        return accountMapper.toDto(account);
    }


    @Override
    @Transactional(readOnly = true)
    public BigDecimal getAccountBalanceByIdForAdmin(Long accountId) {
        String email = currentUserService.getCurrentUserEmail();

        log.info("[ACCOUNT_BALANCE_FETCH_INIT] userEmail={} accountId={}", email,accountId);

        Account account = getAccountByIdOrThrow(accountId);

        log.info("[ACCOUNT_BALANCE_FETCH_SUCCESS] userEmail={} accountId={} balance={}",
                email, account.getId(), account.getBalance());

        return account.getBalance();
    }


    @Override
    @Transactional(readOnly = true)
    public AccountResponseDto getAccountByAliasForAdmin(String alias) {
        String requesterEmail = currentUserService.getCurrentUserEmail();
        String maskedAlias = maskAlias(alias);

        log.info("[ACCOUNT_FETCH_BY_ALIAS_INIT] userEmail={} alias={}", requesterEmail, maskedAlias);

        Account account = getAccountByAliasOrThrow(alias);

       log.info("[ACCOUNT_FETCH_BY_ALIAS_SUCCESS] userEmail={} accountId={} alias={}",
               requesterEmail, account.getId(), maskedAlias);

       return accountMapper.toDto(account);
    }


    @Override
    @Transactional(readOnly = true)
    public AccountResponseDto getMyAccountById(Long accountId) {
        String email = currentUserService.getCurrentUserEmail();

        log.info("[CLIENT_ACCOUNT_FETCH_BY_ID_INIT] userEmail={} accountId={}", email, accountId);

        Account account = getOwnedAccount(accountId, email);

        log.info("[CLIENT_ACCOUNT_FETCH_BY_ID_SUCCESS] userEmail={} accountId={} status={}",
                email, account.getId(), account.getStatusAccount());

        return accountMapper.toDto(account);
    }


    @Override
    @Transactional(readOnly = true)
    public BigDecimal getMyAccountBalance(Long accountId) {
        String email = currentUserService.getCurrentUserEmail();

        log.info("[CLIENT_ACCOUNT_BALANCE_FETCH_INIT] userEmail={} accountId={}", email, accountId);

        Account account = getOwnedAccount(accountId, email);

        log.info("[CLIENT_ACCOUNT_BALANCE_FETCH_SUCCESS] userEmail={} accountId={} balance={}",
                email, account.getId(), account.getBalance());

        return account.getBalance();
    }


    @Override
    @Transactional(readOnly = true)
    public AccountResponseDto getMyAccountByAlias(String alias) {
        String email = currentUserService.getCurrentUserEmail();
        String maskedAlias = maskAlias(alias);

        log.info("[CLIENT_ACCOUNT_FETCH_BY_ALIAS_INIT] userEmail={} alias={}", email, maskedAlias);

        Account account = getOwnedAccountByAlias(alias, email);

        log.info("[CLIENT_ACCOUNT_FETCH_BY_ALIAS_SUCCESS] userEmail={} accountId={} alias={}",
                email, account.getId(), maskedAlias);

        return accountMapper.toDto(account);
    }


    private void validateUserIsActive(Users user) {
        if (user.getUserStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(
                    "Only active users can create bank accounts"
            );
        }
    }

    private void validateAccountId(Long accountId){
        if (accountId == null) {
            throw new BusinessException("Account ID is required");
        }
    }

    private void validateAlias(String alias) {
        if (alias == null || alias.isBlank()) {
            throw new BusinessException("Alias is required");
        }
    }

    private String validateAndNormalizeAlias(String alias) {
        if (alias == null || alias.isBlank()) {
            throw new BusinessException("Alias is required");
        }
        String normalizedAlias = alias.trim().toLowerCase();

        if (normalizedAlias.length() < MIN_ALIAS_LENGTH || normalizedAlias.length() > MAX_ALIAS_LENGTH) {
            throw new BusinessException("Alias length must be between " + MIN_ALIAS_LENGTH + " and " +
                    MAX_ALIAS_LENGTH + " characters");
        }
        return normalizedAlias;
    }



    private void validateAccountCreationRules(Long userId, TypeAccount typeAccount, String alias) {
        if (typeAccount  == null) {
            throw new BusinessException("Account type is required");
        }

        if (accountRepository.existsByUsersIdAndAliasIgnoreCase(userId, alias)) {
            throw new DuplicateResourceException("Alias already in use");
        }

        if (accountRepository.countByUsersId(userId) >= MAX_ACCOUNTS_PER_USER) {
            throw new BusinessException("User has reached the maximum number of accounts");
        }

        if (accountRepository.existsByUsersIdAndTypeAccount(userId, typeAccount)) {
            throw new AccountStateException("User already has an account of this type");
        }
    }



    private void validateAccountCanBeClosed(Account account) {
        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {

            auditLogService.registerEvent(
                    account.getUsers().getId(),
                    "Account closure rejected because balance is greater than zero. accountId= "
                            + account.getId()
                    + ", balance= " + account.getBalance(),
                    AccountAuditAction.ACCOUNT_CLOSE_REJECTED,
                    AuditType.ACCOUNT
            );
            throw new AccountStateException("Account cannot be deleted while balance is greater than zero");
        }

        boolean hasActiveLoans = loanRepository.existsByAccountIdAndLoanStatusIn(
                account.getId(),
                List.of(LoanStatus.ACTIVE, LoanStatus.APPROVE)
        );

        if (hasActiveLoans) {
            auditLogService.registerEvent(
                    account.getUsers().getId(),
                    "Account closure rejected because account has active loans. accountId="
                    + account.getId(),
                    AccountAuditAction.ACCOUNT_CLOSE_REJECTED,
                    AuditType.ACCOUNT
            );

            throw new AccountStateException("Account cannot be closed while it has active loans");
        }
    }

    private Account getAccountByIdOrThrow(Long accountId) {
        validateAccountId(accountId);

        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    private Account getAccountByAliasOrThrow(String alias) {
        validateAlias(alias);

        return accountRepository.findByAlias(alias)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }


    private Account getOwnedAccount(Long accountId, String email) {
        return accountRepository.findByIdAndUsersEmail(accountId,email)
                .orElseThrow(()-> new ResourceNotFoundException("Account not found"));
    }


    private Account getOwnedAccountForUpdate(Long accountId, String email) {
        return accountRepository.findByIdAndUsersEmailForUpdate(accountId, email)
                .orElseThrow(()-> new ResourceNotFoundException("Account not found"));
    }


    private Account getOwnedAccountByAlias(String alias, String email) {
       return accountRepository.findByAliasAndUsersEmail(alias,email)
               .orElseThrow(()-> new ResourceNotFoundException("Account not found"));
    }


    private String generateUniqueAccountNumber() {
        String number;
        do {
            number = generateNumericString(12);
        } while (accountRepository.existsByAccountNumber(number));

        return number;
    }


    private String generateUniqueCbu(){
        String cbu;
        do {
            cbu = generateNumericString(22);
        }while (accountRepository.existsByCBU(cbu));

        return cbu;
    }

    private String generateNumericString (int length) {
        StringBuilder builder = new StringBuilder(length);

        for (int i =0; i< length; i++) {
            builder.append(ThreadLocalRandom.current().nextInt(0,10));
        }
        return builder.toString();
    }

    private String maskAlias(String alias) {
        if (alias == null || alias.length() < 4) return "****";
        return alias.substring(0,2) + "****" + alias.substring(alias.length() - 2);
    }

    private AccountResponseDto replayResponse(IdempotencyRecord record) {
        try {
            return objectMapper.readValue(record.getResponseBody(), AccountResponseDto.class);
        } catch (Exception e) {
            throw new IdempotencyConflictException("Error reconstructing idempotent response");
        }
    }

    private void completeIdempotencyRecord(IdempotencyRecord record, AccountResponseDto responseDto) {
        try {
            String responseBody = objectMapper.writeValueAsString(responseDto);

            idempotencyService.markAsCompleted(
                    record.getId(),
                    201,
                    responseBody,
                    responseDto.getId()
            );
        } catch (Exception e) {
            String errorMessage = e.getMessage() != null
                    ? e.getMessage()
                    : "Unexpected error during create account";

            idempotencyService.markAsFailed(record.getId(), errorMessage);

            throw new IdempotencyConflictException("Could not complete idempotent create account operation");
        }
    }
}
