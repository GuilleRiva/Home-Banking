package com.home_banking_.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.home_banking_.dto.request.*;
import com.home_banking_.dto.response.TransactionResponseDto;
import com.home_banking_.enums.*;
import com.home_banking_.exceptions.BusinessException;
import com.home_banking_.exceptions.IdempotencyConflictException;
import com.home_banking_.exceptions.ResourceNotFoundException;
import com.home_banking_.mappers.TransactionMapper;
import com.home_banking_.model.Account;
import com.home_banking_.model.IdempotencyRecord;
import com.home_banking_.model.Transaction;
import com.home_banking_.repository.AccountRepository;
import com.home_banking_.repository.TransactionRepository;
import com.home_banking_.repository.UsersRepository;
import com.home_banking_.service.AuditLogService;
import com.home_banking_.service.TransactionService;
import com.home_banking_.service.idempotency.IdempotencyService;
import com.home_banking_.service.idempotency.IdempotencyValidationResult;
import com.home_banking_.service.security.CurrentUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TransactionServiceImpl implements TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final UsersRepository usersRepository;
    private final CurrentUserService currentUserService;
    private final AuditLogService auditLogService;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    public TransactionServiceImpl(AccountRepository accountRepository, TransactionRepository transactionRepository, TransactionMapper transactionMapper, UsersRepository usersRepository, CurrentUserService currentUserService, AuditLogService auditLogService, IdempotencyService idempotencyService, ObjectMapper objectMapper) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
        this.usersRepository = usersRepository;
        this.currentUserService = currentUserService;
        this.auditLogService = auditLogService;
        this.idempotencyService = idempotencyService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    @Override
    public TransactionResponseDto makeTransfer(String idempotencyKey, TransactionRequestDto dto) {
        String email = currentUserService.getCurrentUserEmail();
        Long userId = currentUserService.getCurrentUserId();

        IdempotencyValidationResult validationResult = idempotencyService.validateAndRegister(
                idempotencyKey,
                userId,
                IdempotencyOperation.TRANSFER,
                dto
        );

        if (validationResult.isReplay()) {
            log.info("[TRANSFER_IDEMPOTENT_REPLAY] userEmail={} idempotencyKey={}", email, idempotencyKey);
            return replayResponse(validationResult.getRecord());
        }

        if (validationResult.isProcessing()){
            log.warn("[TRANSFER_IDEMPOTENT_PROCESSING] userEmail={} idempotencyKey={}", email, idempotencyKey);
            throw new IdempotencyConflictException("This transfer request is already being processed");
        }

        IdempotencyRecord record = validationResult.getRecord();

        try {
            TransactionResponseDto response = executeTransfer(dto, email,userId);

            String responseBody = serializeResponse(response);

            idempotencyService.markAsCompleted(
                    record.getId(),
                    200,
                    responseBody,
                    response.getId()
            );

            return response;
        }catch (Exception ex) {
            String errorMessage = ex.getMessage() != null ? ex.getMessage() : "Unexpected error during transfer";
            idempotencyService.markAsFailed(record.getId(), errorMessage);
            throw ex;
        }
    }


    private TransactionResponseDto executeTransfer(TransactionRequestDto dto, String email, Long userId) {
        log.info("[TRANSFER_INIT] userEmail={} originAccountId={} destinationAccountId={} amount={}",
                email, dto.getOriginAccountId(), dto.getDestinationAccountId(), dto.getAmount());

        if (dto.getOriginAccountId().equals(dto.getDestinationAccountId())) {
            log.warn("[TRANSFER_REJECTED] Transfer between identical account is not allowed. userEmail={} accountId={}",
                    email, dto.getOriginAccountId());

            auditLogService.registerEvent(
                    userId,
                    "Transfer rejected: origin and destination accounts are the same. accountId=" + dto.getOriginAccountId(),
                    "TRANSFER_REJECTED",
                    "SECURITY"
            );

            throw new BusinessException("Origin and destination accounts must be different");
        }

        BigDecimal amount = dto.getAmount();
        validateAmount(amount);

        Account origin = accountRepository.findByIdAndUsersEmailForUpdate(dto.getOriginAccountId(), email)
                .orElseThrow(() -> {
                    log.warn("[TRANSFER_REJECTED] Origin account not found or access denied. userEmail={} originAccountId={}",
                            email, dto.getOriginAccountId());
                    return new ResourceNotFoundException("Origin account not found");
                });

        Account destination = accountRepository.findByIdForUpdate(dto.getDestinationAccountId())
                .orElseThrow(() -> {
                    log.warn("[TRANSFER_REJECTED] Destination account not found. destinationAccountId={}",
                            dto.getDestinationAccountId());
                    return new ResourceNotFoundException("Destination account not found");
                });

        validateAccountActive(origin, "Origin", "TRANSFER", userId);
        validateAccountActive(destination, "Destination", "TRANSFER", userId);

        if (origin.getBalance().compareTo(amount) < 0) {
            log.warn("[TRANSFER_REJECTED] Insufficient balance. originAccountId={} balance={} requiredAmount={}",
                    origin.getId(), origin.getBalance(), amount);

            auditLogService.registerEvent(
                    userId,
                    "Transfer rejected due to insufficient balance. originAccountId=" + origin.getId()
                            + ", destinationAccountId=" + destination.getId()
                            + ", amount=" + amount,
                    "TRANSFER_REJECTED",
                    "SECURITY"
            );

            throw new BusinessException("Insufficient balance for transfer");
        }

        origin.setBalance(origin.getBalance().subtract(amount));
        destination.setBalance(destination.getBalance().add(amount));

        Transaction tx = buildTransferTransaction(origin, destination, amount);
        Transaction savedTx = transactionRepository.save(tx);

        auditLogService.registerEvent(
                userId,
                "Transfer completed successfully. transactionId=" + savedTx.getId()
                        + ", originAccountId=" + origin.getId()
                        + ", destinationAccountId=" + destination.getId()
                        + ", amount=" + amount,
                "TRANSFER_COMPLETED",
                "TRANSACTION"
        );

        log.info("[TRANSFER_SUCCESS] transactionId={} originAccountId={} destinationAccountId={} amount={}",
                savedTx.getId(), origin.getId(), destination.getId(), amount);

        return transactionMapper.toDto(savedTx);
    }


    @Transactional
    @Override
    public TransactionResponseDto makeWithdraw(WithDrawRequestDto dto) {
        String email = currentUserService.getCurrentUserEmail();
        Long userId = currentUserService.getCurrentUserId();
        log.info("[WITHDRAW_INIT] userEmail={} accountId={} amount={}", email, dto.getAccountId(), dto.getAmount());

        BigDecimal amount = dto.getAmount();
        validateAmount(amount);

        Account account = accountRepository.findByIdAndUsersEmailForUpdate(dto.getAccountId(), email)
                        .orElseThrow(()-> {
                            log.warn("[WITHDRAW_REJECTED] Account not found or access denied. userEmail={} accountId={}",
                                    email,dto.getAccountId());
                            return new ResourceNotFoundException("Account not found");
                        });

        validateAccountActive(account, "Account", "WITHDRAW", userId);

        if (account.getBalance().compareTo(amount) < 0) {
            log.warn("[WITHDRAW_REJECTED] Insufficient balance. accountId={} balance={} requiredAmount={}",
                    account.getId(), account.getBalance(), amount);

            auditLogService.registerEvent(
                    userId,
                    "Withdraw rejected due to insufficient balance. accountId=" + account.getId()
                            + ", balance=" + account.getBalance()
                            + ", amount=" + amount,
                    "WITHDRAW_REJECTED",
                    "SECURITY"
            );
            throw new BusinessException("Insufficient balance for withdrawal");
        }

        account.setBalance(account.getBalance().subtract(amount));

        Transaction tx = buildWithdrawTransaction(account,amount);
        transactionRepository.save(tx);

        auditLogService.registerEvent(
                userId,
                "Withdraw completed successfully. transactionId=" + tx.getId()
                + ", accountId=" + account.getId()
                + ", amount=" + amount,
                "WITHDRAW_COMPLETED",
                "TRANSACTION"
        );

        log.info("[WITHDRAW_SUCCESS] transactionId={} accountId={} amount={} newBalance={}",
                tx.getId(), account.getId(), amount, account.getBalance());

        return transactionMapper.toDto(tx);
    }

    @Transactional
    @Override
    public TransactionResponseDto makeCustomerDeposit(CustomerDepositRequestDto dto) {
        String email = currentUserService.getCurrentUserEmail();

        log.info("[CUSTOMER_DEPOSIT_INIT] userEmail={} accountId={} amount={}",
                email, dto.getAccountId(), dto.getAmount());

        Account account = getOwnedAccountForUpdate(dto.getAccountId(),email);
        validateAccountActive(account, "Account", "CUSTOMER_DEPOSIT", account.getId());
        validateAmount(dto.getAmount());

        applyCreditToAccount(account, dto.getAmount());

        Transaction transaction = buildCustomerDepositTransaction(account, dto);
        Transaction savedTransaction = transactionRepository.save(transaction);
        accountRepository.save(account);

        auditLogService.registerEvent(
                account.getUsers().getId(),
                "Customer deposit completed. transactionId=" + savedTransaction.getId()
                + ", accountId=" + account.getId()
                + ", amount=" + dto.getAmount(),
                "CUSTOMER_DEPOSIT_COMPLETED",
                "TRANSACTION"
        );

        log.info("[CUSTOMER_DEPOSIT_SUCCESS] userEmail={} accountId={} transactionId={} amount={}",
                email, account.getId(), savedTransaction.getId(), savedTransaction.getAmount());

        return transactionMapper.toDto(savedTransaction);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYED')")
    @Override
    public TransactionResponseDto makeAdministrativeCredit(AdministrativeCreditRequestDto dto) {
        Long adminUserId= currentUserService.getCurrentUserId();

        log.info("[ADMIN_CREDIT_INIT] accountId={} amount={} reason={}",
                dto.getAccountId(), dto.getAmount(), dto.getReason());

        Account account = getAccountForUpdate(dto.getAccountId());
        validateAccountActive(account, "Account", "ADMIN_CREDIT", dto.getAccountId());
        validateAmount(dto.getAmount());
        validateReason(dto.getReason());

        applyCreditToAccount(account, dto.getAmount());

        Transaction transaction = buildAdministrativeCreditTransaction(account, dto);
        Transaction savedTransaction = transactionRepository.save(transaction);
        accountRepository.save(account);

        auditLogService.registerEvent(
                adminUserId,
                "Administrative credit completed. transactionId=" + savedTransaction.getId()
                + ", targetAccountId=" + account.getId()
                + ", amount=" + dto.getAmount()
                + ", reason=" + dto.getReason(),
                "ADMIN_CREDIT_COMPLETED",
                "AUDIT"
        );

        log.info("[ADMIN_CREDIT_SUCCESS] accountId={} transactionId={} amount={}",
                account.getId(), savedTransaction.getId(), savedTransaction.getAmount());

        return transactionMapper.toDto(savedTransaction);
    }

    @Transactional(readOnly = true)
    @Override
    public List<TransactionResponseDto> getTransactionsByAccount(Long accountId) {
        String email = currentUserService.getCurrentUserEmail();

        log.info("[FETCH_ACCOUNT_TRANSACTIONS_INIT] userEmail={} accountId={}",  email,accountId);

        accountRepository.findByIdAndUsersEmail(accountId, email)
                .orElseThrow(()->{
                    log.warn("[FETCH_ACCOUNT_TRANSACTIONS_REJECTED] account not found or access denied. userEmail={} accountId={}",
                            email, accountId);
                    return new ResourceNotFoundException("Account not found");
                });

        List<Transaction> transactions = transactionRepository.findMyTransactionsByAccount(email, accountId);

        log.info("[FETCH_ACCOUNT_TRANSACTIONS_SUCCESS] userEmail={} accountId={} transactionSize={}"
                ,email, accountId, transactions.size());

        return transactions.stream()
                .map(transactionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<TransactionResponseDto> getMyTransactions() {
        String email = currentUserService.getCurrentUserEmail();

        log.info("[FETCH_MY_TRANSACTIONS_INIT] userEmail={}", email);

        List<Transaction> txs = transactionRepository.findMyTransactions(email);

        log.info("[FETCH_MY_TRANSACTIONS_SUCCESS] userEmail={} transactionsSize={}", email, txs.size());

        return txs.stream()
                .map(transactionMapper::toDto)
                .toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYED')")
    @Transactional(readOnly = true)
    @Override
    public List<TransactionResponseDto> getTransactionsByUser(Long userId) {
        String requesterEmail = currentUserService.getCurrentUserEmail();
        Long requestUserId = currentUserService.getCurrentUserId();

        log.info("[FETCH_USER_TRANSACTIONS_INIT] requesterEmail={} requestedUserId={}", requesterEmail, userId);

       if (!usersRepository.existsById(userId)) {
           log.warn("[FETCH_USER_TRANSACTIONS_REJECTED] requested user not found. requesterEmail={} requestedUserId={}",
                   requesterEmail, userId);
           throw new ResourceNotFoundException("User not found");
       }

        List<Transaction> transactions = transactionRepository.findAllByUserId(userId);

        auditLogService.registerEvent(
                requestUserId,
                "Transaction history requested for userId=" + userId,
                "USER_TRANSACTIONS_VIEWED",
                "AUDIT"
        );

        log.info("[FETCH_USER_TRANSACTIONS_SUCCESS] requesterEmail={} requestedUserId={} transactionSize={}",
                requesterEmail, userId, transactions.size());

        return transactions.stream()
                .map(transactionMapper::toDto)
                .collect(Collectors.toList());
    }
    // -------------------
    // Helpers
    // ---------------------------

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("[AMOUNT_VALIDATION_REJECTED] amount={} reason=Invalid amount", amount);
            throw new BusinessException("Amount must be positive");
        }
        if (amount.scale() > 2) {
            throw new BusinessException("Amount must have at most 2 decimal places");
        }
    }

    private void validateAccountActive(Account acc, String label, String operation, Long userId) {
        if (acc.getStatusAccount() != StatusAccount.ACTIVE) {
            log.warn("[{}_REJECTED] {} account is not active. userId={} accountId={} status={}",
                    operation, label.toUpperCase(), userId, acc.getId(), acc.getStatusAccount());

            auditLogService.registerEvent(
                    userId,
                    operation + " rejected because " + label.toLowerCase() + " account is not active. accountId="
                    + acc.getId() + ", status=" + acc.getStatusAccount(),
                    operation + "_REJECTED",
                    "SECURITY"
            );
            throw new BusinessException(label + " account is not active");
        }
    }

    private void validateReason(String reason) {
        if (reason== null || reason.trim().isEmpty()) {
            log.warn("[ADMIN_CREDIT_REJECTED] reason=Administrative reason is required");
            throw new BusinessException("A reason is required for administrative credit");
        }

        if (reason.trim().length() < 5){
            log.warn("[ADMIN_CREDIT_REJECTED] reason= Administrative reason too short");
            throw new BusinessException("The reason must contain at least 5 characters");
        }
    }

    private Transaction buildTransferTransaction(Account origin, Account destination, BigDecimal amount) {
        Transaction tx = new Transaction();
        tx.setAmount(amount);
        tx.setAccountOrigin(origin);
        tx.setAccountDestiny(destination);
        tx.setCreationDate(LocalDateTime.now());
        tx.setStatusTransaction(StatusTransaction.COMPLETED);
        tx.setTypeTransaction(TransactionOperationType.TRANSFER);
        return tx;
    }

    private Transaction buildDepositTransaction(Account account, BigDecimal amount) {
        Transaction tx = new Transaction();
        tx.setAmount(amount);
        tx.setAccountDestiny(account);
        tx.setCreationDate(LocalDateTime.now());
        tx.setStatusTransaction(StatusTransaction.COMPLETED);
        tx.setTypeTransaction(TransactionOperationType.DEPOSIT);
        return tx;
    }

    private Transaction buildWithdrawTransaction(Account account, BigDecimal amount) {
        Transaction tx = new Transaction();
        tx.setAmount(amount);
        tx.setAccountOrigin(account);
        tx.setCreationDate(LocalDateTime.now());
        tx.setStatusTransaction(StatusTransaction.COMPLETED);
        tx.setTypeTransaction(TransactionOperationType.WITHDRAW);
        return tx;
    }

    private Transaction buildAdministrativeCreditTransaction (Account account, AdministrativeCreditRequestDto dto) {
        Transaction transaction = new Transaction();
        transaction.setAccountOrigin(null);
        transaction.setAccountDestiny(account);
        transaction.setAmount(dto.getAmount());
        transaction.setCreationDate(LocalDateTime.now());
        transaction.setTypeTransaction(TransactionOperationType.ADMIN_CREDIT);
        transaction.setStatusTransaction(StatusTransaction.COMPLETED);
        transaction.setDescription(dto.getDescription() + " | Reason: " + dto.getReason());

        return transaction;
    }

    private Transaction buildCustomerDepositTransaction(Account account, CustomerDepositRequestDto dto) {
        Transaction transaction = new Transaction();
        transaction.setAccountOrigin(null);
        transaction.setAccountDestiny(account);
        transaction.setAmount(dto.getAmount());
        transaction.setCreationDate(LocalDateTime.now());
        transaction.setMovementAccountType(MovementAccountType.CREDITO);
        transaction.setStatusTransaction(StatusTransaction.COMPLETED);
        transaction.setTypeTransaction(TransactionOperationType.CUSTOMER_DEPOSIT);
        transaction.setDescription(dto.getDescription());

        return transaction;
    }

    private Account getOwnedAccountForUpdate(Long accountId, String email) {
        return accountRepository.findByIdAndUsersEmailForUpdate(accountId, email)
                .orElseThrow(()-> new ResourceNotFoundException("Account not found"));
    }

    private Account getAccountForUpdate(Long accountId){
        return accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(()-> new ResourceNotFoundException("Account not found"));
    }

    private void applyCreditToAccount(Account account, BigDecimal amount) {
        account.setBalance(account.getBalance().add(amount));
    }

    private String serializeResponse(TransactionResponseDto response) {
        try {
            return objectMapper.writeValueAsString(response);
        }catch (Exception e) {
            throw new BusinessException("Error serializing transaction response");
        }
    }

    private TransactionResponseDto replayResponse(IdempotencyRecord record) {
        try {
            return objectMapper.readValue(record.getResponseBody(), TransactionResponseDto.class);
        } catch (Exception e) {
            throw new BusinessException("Error reconstructing idempotent response");
        }
    }
}
