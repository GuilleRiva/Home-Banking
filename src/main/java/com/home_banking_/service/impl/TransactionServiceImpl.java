package com.home_banking_.service.impl;

import com.home_banking_.dto.request.DepositRequestDto;
import com.home_banking_.dto.request.TransactionRequestDto;
import com.home_banking_.dto.request.WithDrawRequestDto;
import com.home_banking_.dto.response.TransactionResponseDto;
import com.home_banking_.enums.StatusAccount;
import com.home_banking_.enums.StatusTransaction;
import com.home_banking_.enums.TransactionOperationType;
import com.home_banking_.exceptions.BusinessException;
import com.home_banking_.exceptions.ResourceNotFoundException;
import com.home_banking_.mappers.TransactionMapper;
import com.home_banking_.model.Account;
import com.home_banking_.model.Transaction;
import com.home_banking_.model.Users;
import com.home_banking_.repository.AccountRepository;
import com.home_banking_.repository.TransactionRepository;
import com.home_banking_.repository.UsersRepository;
import com.home_banking_.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
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

    public TransactionServiceImpl(AccountRepository accountRepository, TransactionRepository transactionRepository, TransactionMapper transactionMapper, UsersRepository usersRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
        this.usersRepository = usersRepository;
    }

    @Transactional
    @Override
    public TransactionResponseDto makeTransfer(TransactionRequestDto dto) {

        // Email del JWT
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // Validaciones básica
        if (dto.getOriginAccountId().equals(dto.getDestinationAccountId())) {
            throw new BusinessException("Origin and destination accounts must be different");
        }


        BigDecimal amount = dto.getAmount();
        validateAmount(amount);

        // Origen debe pertenecer al usuario autenticado
        Account origin = accountRepository.findByIdAndUsersEmailForUpdate(dto.getOriginAccountId(), email)
                .orElseThrow(() -> new ResourceNotFoundException("Origin account not found"));

        Account destination = accountRepository.findByIdForUpdate(dto.getDestinationAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination account not found"));

        validateAccountActive(origin, "Origin");
        validateAccountActive(destination, "Destination");

        // Reglas de negocio

        if (origin.getBalance().compareTo(amount) < 0) {
            log.warn("Insufficient balance. originId={} balance={} amount={}",
                    origin.getId(), origin.getBalance(), amount);
            throw new BusinessException("Insufficient balance for transfer");
        }

        //Aplicar movimientos
        origin.setBalance(origin.getBalance().subtract(amount));
        destination.setBalance(destination.getBalance().add(amount));

        //Registrar transacción
        Transaction tx = new Transaction();
        tx.setAmount(amount);
        tx.setAccountOrigin(origin);
        tx.setAccountDestiny(destination);
        tx.setCreationDate(LocalDateTime.now());
        tx.setStatusTransaction(StatusTransaction.COMPLETED);
        tx.setTypeTransaction(TransactionOperationType.TRANSFER);

        transactionRepository.save(tx);

        log.info("Transfer OK: origin={} dest={} amount={} txId={}",
                origin.getId(), destination.getId(), amount, tx.getId());

        return transactionMapper.toDto(tx);
    }

    @Transactional
    @Override
    public TransactionResponseDto makeDeposit(DepositRequestDto dto) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        BigDecimal amount = dto.getAmount();
        validateAmount(amount);

        Account account = accountRepository.findByIdAndUsersEmailForUpdate(dto.getAccountId(), email)
                .orElseThrow(()-> new ResourceNotFoundException("Account not found"));

        validateAccountActive(account, "Account");

        account.setBalance(account.getBalance().add(amount));

        Transaction tx = new Transaction();
        tx.setAmount(amount);
        tx.setAccountDestiny(account); // depósito entra (destino)
        tx.setCreationDate(LocalDateTime.now());
        tx.setStatusTransaction(StatusTransaction.COMPLETED);
        tx.setTypeTransaction(TransactionOperationType.DEPOSIT);

        transactionRepository.save(tx);

        log.info("Deposit OK: account={} amount={} txId={}", account.getId(), amount, tx.getId());

        return transactionMapper.toDto(tx);
    }

    @Transactional
    @Override
    public TransactionResponseDto makeWithdraw(WithDrawRequestDto dto) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        BigDecimal amount = dto.getAmount();
        validateAmount(amount);

        Account account = accountRepository.findByIdAndUsersEmail(dto.getAccountId(), email)
                .orElseThrow(()-> new ResourceNotFoundException("Account not found"));

        validateAccountActive(account, "Account");

        if (account.getBalance().compareTo(amount) < 0) {
            log.warn("Insufficient balance. accountId={} balance={} amount={}",
                    account.getId(), account.getBalance(), amount);
            throw new BusinessException("Insufficient balance for withdraw");
        }

        account.setBalance(account.getBalance().subtract(amount));

        Transaction tx = new Transaction();
        tx.setAmount(amount);
        tx.setAccountOrigin(account); // retiro sale (origen)
        tx.setCreationDate(LocalDateTime.now());
        tx.setStatusTransaction(StatusTransaction.COMPLETED);
        tx.setTypeTransaction(TransactionOperationType.WITHDRAW);

        transactionRepository.save(tx);

        log.info("Withdraw OK: account={} amount={} txId={}", account.getId(), amount, tx.getId());

        return transactionMapper.toDto(tx);
    }

    @Transactional(readOnly = true)
    @Override
    public List<TransactionResponseDto> getTransactionsByAccount(Long accountId) {
        log.info("Querying transactions for ID account: {}", accountId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(()->  new ResourceNotFoundException(
                        "Account not found"
                ));

        List<Transaction> transactions = transactionRepository.findByAccountId(accountId);

        log.info("They found each other {} Transactions associated with the ID account: {}", transactions.size(), accountId);

        return transactions.stream()
                .map(transactionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponseDto> getMyTransactionsByAccount(Long accountId) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

       return transactionRepository.findMyTransactionsByAccount(email, accountId).stream()
               .map(transactionMapper::toDto)
               .toList();
    }

    @Override
    public List<TransactionResponseDto> getMyTransactions() {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        List<Transaction> txs = transactionRepository.findMyTransactions(email);
        return txs.stream()
                .map(transactionMapper::toDto)
                .toList();
    }


    @Transactional(readOnly = true)
    public List<TransactionResponseDto> getTransactionsByUser(Long userId) {
        log.info("Querying transactions for user ID: {}", userId);

        Users users = usersRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException(
                        "User not found"
                ));

        List<Transaction> transactions = transactionRepository.findAllByUserId(userId);

        log.info("They found each other {} Transactions associated with user ID: {}", transactions.size(), userId);

        return transactions.stream()
                .map(transactionMapper::toDto)
                .collect(Collectors.toList());

    }

    // -------------------
    // Helpers
    // ---------------------------

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Amount must be positive");
        }
        if (amount.scale() > 2) {
            throw new BusinessException("Amount must have at most 2 decimal places");
        }
    }

    private void validateAccountActive(Account acc, String label) {
        if (acc.getStatusAccount() != StatusAccount.ACTIVE) {
            throw new BusinessException(label + "account is not active");
        }
    }
}
