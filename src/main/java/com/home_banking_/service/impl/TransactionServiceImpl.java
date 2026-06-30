package com.home_banking_.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.home_banking_.dto.request.*;
import com.home_banking_.dto.response.TransactionResponseDto;
import com.home_banking_.enums.*;
import com.home_banking_.enums.audit.AuditType;
import com.home_banking_.enums.audit.TransactionAuditAction;
import com.home_banking_.exceptions.custom.*;
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
import org.springframework.http.HttpStatus;
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
    public TransactionResponseDto makeTransfer(String idempotencyKey, TransactionRequestDto dto)  {
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

        TransactionResponseDto response = idempotencyService.executeAndComplete(
                validationResult.getRecord(),
                () -> executeTransfer(dto,email,userId),
                TransactionResponseDto::getId,
                HttpStatus.CREATED,
                "Unexpected error during transfer"
        );

        log.info("[TRANSFER_IDEMPOTENT_COMPLETED] userEmail={} transactionId={} recordId={}",
                email, response.getId(), validationResult.getRecord());

        return response;
    }


    private TransactionResponseDto executeTransfer(TransactionRequestDto dto, String email, Long userId) {

        log.info("[TRANSFER_INIT] userEmail={} originAccountId={} destinationAccountId={} amount={}",
                email, dto.getOriginAccountId(), dto.getDestinationAccountId(), dto.getAmount());

        validateTransferRequest(dto,email,userId);

        BigDecimal amount = dto.getAmount();

        Account origin = getOwnedAccountForUpdate(dto.getOriginAccountId(),email);
        Account destination = getAccountForUpdate(dto.getDestinationAccountId());

        validateAccountsCanTransfer(origin,destination,amount,userId);

        applyTransfer(origin,destination,amount);
        
        Transaction savedTx= saveTransferTransaction(origin,destination,amount);

        registerTransferCompletedAudit(userId,savedTx,origin,destination,amount);

        log.info("[TRANSFER_SUCCESS] transactionId={} originAccountId={} destinationAccountId={} amount={}",
                savedTx.getId(), origin.getId(), destination.getId(), amount);

        return transactionMapper.toDto(savedTx);
    }


    @Transactional
    @Override
    public TransactionResponseDto makeWithdraw(String idempotencyKey, WithDrawRequestDto dto)  {
        String email = currentUserService.getCurrentUserEmail();
        Long userId = currentUserService.getCurrentUserId();

        IdempotencyValidationResult validationResult = idempotencyService.validateAndRegister(
                idempotencyKey,
                userId,
                IdempotencyOperation.WITHDRAW,
                dto
        );

        if (validationResult.isReplay()) {
            log.info("[MAKE_WITHDRAW_IDEMPOTENT_REPLAY] userEmail={} idempotencyKey={}", email, idempotencyKey);
            return replayResponse(validationResult.getRecord());
        }

        if (validationResult.isProcessing()) {
            log.warn("[MAKE_WITHDRAW_IDEMPOTENT_PROCESSING] userEmail={} idempotencyKey={}", email, idempotencyKey);
            throw new IdempotencyConflictException("This withdraw request is already being processed");
        }

        return idempotencyService.executeAndComplete(
                validationResult.getRecord(),
                ()-> executeWithdraw(dto,email,userId),
                TransactionResponseDto::getId,
                HttpStatus.CREATED,
                "Unexpected error during withdraw"
        );

    }

    private TransactionResponseDto executeWithdraw(WithDrawRequestDto dto, String email, Long userId){
        log.info("[WITHDRAW_INIT] userEmail={} accountId={} amount={}", email, dto.getAccountId(), dto.getAmount());

        validateWithdrawRequest(dto);

        BigDecimal amount = dto.getAmount();

        Account account = getOwnedAccountForUpdate(dto.getAccountId(), email);

        validateAccountCanWithdraw(account, amount, userId);

        debitAccount(account,amount);

        Transaction savedTx = saveWithdrawTransaction(account,amount);

        registerWithdrawCompletedAudit(userId,savedTx,account,amount);

        log.info("[WITHDRAW_SUCCESS] transactionId={} accountId={} amount={} newBalance={}",
                savedTx.getId(), account.getId(), amount, account.getBalance());

        return transactionMapper.toDto(savedTx);

    }


    @Transactional
    @Override
    public TransactionResponseDto makeCustomerDeposit(String idempotencyKey, CustomerDepositRequestDto dto)  {
        String email = currentUserService.getCurrentUserEmail();
        Long userId= currentUserService.getCurrentUserId();

        IdempotencyValidationResult validationResult = idempotencyService.validateAndRegister(
                idempotencyKey,
                userId,
                IdempotencyOperation.DEPOSIT,
                dto
        );

        if (validationResult.isReplay()) {
            log.info("[CUSTOMER_DEPOSIT_IDEMPOTENT_REPLAY] userEmail={} idempotency={}", email, idempotencyKey);
            return replayResponse(validationResult.getRecord());
        }

        if (validationResult.isProcessing()) {
            log.warn("[CUSTOMER_DEPOSIT_IDEMPOTENT_PROCESSING] userEmail={} idempotencyKey={}", email, idempotencyKey);
            throw new IdempotencyConflictException("This deposit request is already being processed");
        }

        return idempotencyService.executeAndComplete(
                validationResult.getRecord(),
                ()-> executeCustomerDeposit(dto,email,userId),
                TransactionResponseDto::getId,
                HttpStatus.CREATED,
                "Unexpected error during customer deposit"
        );
    }


    private TransactionResponseDto executeCustomerDeposit (CustomerDepositRequestDto dto, String email, Long userId) {
        log.info("[CUSTOMER_DEPOSIT_INIT] userEmail={} accountId={} amount={}",
                email, dto.getAccountId(), dto.getAmount());

        Account account = getOwnedAccountForUpdate(dto.getAccountId(), email);
        validateAccountActive(account, "Account", "CUSTOMER_DEPOSIT", userId);
        validateAmount(dto.getAmount());

        applyCreditToAccount(account, dto.getAmount());

        Transaction transaction = buildCustomerDepositTransaction(account, dto);
        Transaction savedTransaction = transactionRepository.save(transaction);
        accountRepository.save(account);

        auditLogService.registerEvent(
                account.getUsers().getId(),
                "Customer deposit completed. transactionId=" + savedTransaction.getId()
                + ", accountId= " + account.getId()
                + ", amount= " + dto.getAmount(),
                TransactionAuditAction.CUSTOMER_DEPOSIT_COMPLETED,
                AuditType.TRANSACTION
        );

        log.info("[CUSTOMER_DEPOSIT_SUCCESS] userEmail={} accountId={} transactionId={} amount={}",
                email, account.getId(), savedTransaction.getId(),savedTransaction.getAmount());

        return transactionMapper.toDto(savedTransaction);
    }


    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYED')")
    @Override
    public TransactionResponseDto makeAdministrativeCredit(String idempotencyKey, AdministrativeCreditRequestDto dto) {
        String email = currentUserService.getCurrentUserEmail();
        Long userId= currentUserService.getCurrentUserId();

        IdempotencyValidationResult validationResult = idempotencyService.validateAndRegister(
                idempotencyKey,
                userId,
                IdempotencyOperation.LOAN_GRANT,
                dto
        );

        if (validationResult.isReplay()) {
            log.info("[ADMINISTRATIVE_CREDIT_IDEMPOTENT_REPLAY] userEmail={} idempotencyKey={}", email, idempotencyKey);
            return replayResponse(validationResult.getRecord());
        }

        if (validationResult.isProcessing()) {
            log.warn("[ADMINISTRATIVE_CREDIT_IDEMPOTENT_PROCESSING] userEmail={} idempotencyKey={}", email, idempotencyKey);
            throw new IdempotencyConflictException("This administrative credit request is already being processed");
        }

      return idempotencyService.executeAndComplete(
              validationResult.getRecord(),
              ()-> executeAdministrativeCredit(dto,email,userId),
              TransactionResponseDto::getId,
              HttpStatus.CREATED,
              "Unexpected error during administrative credit"
      );

    }

    private TransactionResponseDto executeAdministrativeCredit(AdministrativeCreditRequestDto dto, String email, Long userId) {
        log.info("[ADMIN_CREDIT_INIT] userEmail={} accountId={} amount={} reason={}",
                email, dto.getAccountId(), dto.getAmount(), dto.getReason());

        Account account = getAccountForUpdate(dto.getAccountId());
        validateAccountActive(account, "Account", "ADMIN_CREDIT", userId);
        validateAmount(dto.getAmount());
        validateReason(dto.getReason());

        applyCreditToAccount(account, dto.getAmount());

        Transaction transaction = buildAdministrativeCreditTransaction(account, dto);
        Transaction savedTransaction = transactionRepository.save(transaction);
        accountRepository.save(account);

        auditLogService.registerEvent(
                userId,
                "Administrative credit completed. transactionId= " + savedTransaction.getId()
                        + ", targetAccountId= " + account.getId()
                        + ", amount= " + dto.getAmount()
                        + ", reason= " + dto.getReason(),
                TransactionAuditAction.ADMIN_CREDIT_COMPLETED,
                AuditType.TRANSACTION
        );

        log.info("[ADMIN_CREDIT_SUCCESS] userEmail={}  accountId={} transactionId={} amount={}",
                 email ,account.getId(), savedTransaction.getId(), savedTransaction.getAmount());

        return transactionMapper.toDto(savedTransaction);
    }


    @Transactional(readOnly = true)
    @Override
    public List<TransactionResponseDto> getTransactionsByAccount(Long accountId) {
        String email = currentUserService.getCurrentUserEmail();

        log.info("[FETCH_ACCOUNT_TRANSACTIONS_INIT] userEmail={} accountId={}",  email,accountId);

        Account account = getOwnedAccountForUpdate(accountId, email);

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

       Account account= getOwnedAccountForUpdate(requestUserId, requesterEmail);

        List<Transaction> transactions = transactionRepository.findAllByUserId(userId);

        auditLogService.registerEvent(
                requestUserId,
                "Transaction history requested for userId= " + userId,
                TransactionAuditAction.USER_TRANSACTION_VIEWED,
                AuditType.TRANSACTION
        );

        log.info("[FETCH_USER_TRANSACTIONS_SUCCESS] requesterEmail={} requestedUserId={} transactionSize={}",
                requesterEmail, userId, transactions.size());

        return transactions.stream()
                .map(transactionMapper::toDto)
                .collect(Collectors.toList());
    }


    private void debitAccount(Account account, BigDecimal amount) {
        account.setBalance(account.getBalance().subtract(amount));
    }


    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("[AMOUNT_VALIDATION_REJECTED] amount={} reason=Invalid amount", amount);
            throw new InsufficientFundsException("Amount must be positive");
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
                    + acc.getId() + ", status= " + acc.getStatusAccount(),
                    TransactionAuditAction.TRANSACTION_REJECTED,
                    AuditType.TRANSACTION
            );
            throw new AccountStateException(label + " account is not active");
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

    private void validateTransferRequest(TransactionRequestDto dto, String email, Long userId) {

        validateAmount(dto.getAmount());

        if (dto.getOriginAccountId() == null) {
            throw new AccountStateException("Origin account ID is required");
        }
        if (dto.getOriginAccountId().equals(dto.getDestinationAccountId())) {
            log.warn("[TRANSFER_REJECTED] reason=same_account userEmail={} accountId={}",
                    email, dto.getOriginAccountId());

            auditLogService.registerEvent(
                    userId,
                    "Transfer rejected: origin and destination accounts are the same. accountId= " +
                            dto.getOriginAccountId(),
                    TransactionAuditAction.TRANSACTION_REJECTED,
                    AuditType.TRANSACTION
            );
            throw new InvalidTransactionException("Origin and destination accounts must be different");
        }
    }

    private void validateWithdrawRequest(WithDrawRequestDto dto) {
        if (dto.getAccountId() == null) {
            throw new AccountStateException("Account ID is required");
        }
        validateAmount(dto.getAmount());
    }

    private void validateAccountsCanTransfer(Account origin, Account destination, BigDecimal amount, Long userId){
        validateAccountActive(origin,"Origin", "TRANSFER", userId);
        validateAccountActive(destination, "Destination", "TRANSFER", userId);
        validateSufficientBalanceForTransfer(origin,destination,amount,userId);
    }

    private void validateAccountCanWithdraw(Account account, BigDecimal amount, Long userId) {
        validateAccountActive(account, "Account", "WITHDRAW", userId);
        validateSufficientBalanceForWithdraw(account,amount,userId);
    }

    private void validateSufficientBalanceForTransfer(
            Account origin,
            Account destination,
            BigDecimal amount,
            Long userId
    ) {
        if (origin.getBalance().compareTo(amount) < 0) {
            log.warn("[TRANSFER_REJECTED] reason=insufficient_balance originAccountId={} balance={} requiredAmount={}",
                    origin.getId(), origin.getBalance(),amount);

            auditLogService.registerEvent(
                    userId,
                    "Transfer rejected due to insufficient balance. originAccountId= " + origin.getId()
                    + ", destinationAccountId= " + destination.getId()
                    + ", amount= " + amount,
                    TransactionAuditAction.TRANSACTION_REJECTED_INSUFFICIENT_BALANCE,
                    AuditType.TRANSACTION
            );

            throw new InsufficientFundsException("Insufficient balance for transfer");
        }
    }

    private void validateSufficientBalanceForWithdraw(Account account, BigDecimal amount, Long userId){
        if (account.getBalance().compareTo(amount) < 0) {
            log.warn("[WITHDRAW_REJECTED] reason=insufficient_balance accountId={} balance={} requiredAmount={}",
                    account.getId(),account.getBalance(),amount);

            auditLogService.registerEvent(
                    userId,
                    "Withdraw rejected due to insufficient balance. accountId= " + account.getId()
                    + ", balance= " + account.getBalance()
                    + ", amount= " + amount,
                    TransactionAuditAction.WITHDRAW_REJECTED_INSUFFICIENT_BALANCE,
                    AuditType.TRANSACTION
            );

            throw new InsufficientFundsException("Insufficient balance for withdraw");
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
        tx.setMovementAccountType(MovementAccountType.CUENTA_PROPIA);
        return tx;
    }


    private Transaction buildWithdrawTransaction(Account account, BigDecimal amount) {
        Transaction tx = new Transaction();
        tx.setAmount(amount);
        tx.setAccountOrigin(account);
        tx.setCreationDate(LocalDateTime.now());
        tx.setStatusTransaction(StatusTransaction.COMPLETED);
        tx.setTypeTransaction(TransactionOperationType.WITHDRAW);
        tx.setMovementAccountType(MovementAccountType.CUENTA_PROPIA);
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
        transaction.setMovementAccountType(MovementAccountType.CREDITO);

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

    private void applyTransfer(Account origin, Account destination, BigDecimal amount){
        origin.setBalance(origin.getBalance().subtract(amount));
        destination.setBalance(destination.getBalance().add(amount));
    }

    private void applyCreditToAccount(Account account, BigDecimal amount) {
        account.setBalance(account.getBalance().add(amount));
    }

    private Transaction saveTransferTransaction(Account origin, Account destination, BigDecimal amount) {
        Transaction tx = buildTransferTransaction(origin,destination,amount);
        return transactionRepository.save(tx);
    }

    private Transaction saveWithdrawTransaction(Account account, BigDecimal amount) {
        Transaction tx = buildWithdrawTransaction(account,amount);
        return transactionRepository.save(tx);
    }

    private void registerTransferCompletedAudit(
            Long userId,
            Transaction savedTx,
            Account origin,
            Account destination,
            BigDecimal amount
    ) {
        auditLogService.registerEvent(
                userId,
                "Transfer completed successfully. transactionId= " + savedTx.getId()
                + ", originAccountId= " + origin.getId()
                + ", destinationAccountId= " + destination.getId()
                + ", amount= " + amount,
                TransactionAuditAction.TRANSFER_COMPLETED,
                AuditType.TRANSACTION
        );
    }

    private void registerWithdrawCompletedAudit(Long userId, Transaction savedTx, Account account, BigDecimal amount) {
        auditLogService.registerEvent(
                userId,
                "Withdraw completed successfully. transactionId= " + savedTx.getId()
                + ", accountId= " + account.getId()
                + ", amount= " + amount,
                TransactionAuditAction.WITHDRAW_COMPLETED,
                AuditType.TRANSACTION
        );
    }


    private TransactionResponseDto replayResponse(IdempotencyRecord record) {
        try {
            return objectMapper.readValue(record.getResponseBody(), TransactionResponseDto.class);
        } catch (Exception e) {
            throw new BusinessException("Error reconstructing idempotent response");
        }
    }
}
