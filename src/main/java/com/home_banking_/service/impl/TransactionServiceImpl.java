package com.home_banking_.service.impl;

import com.home_banking_.enums.StatusTransaction;
import com.home_banking_.enums.TypeTransaction;
import com.home_banking_.exceptions.BusinessException;
import com.home_banking_.exceptions.ResourceNotFoundException;
import com.home_banking_.model.Account;
import com.home_banking_.model.Transaction;
import com.home_banking_.repository.AccountRepository;
import com.home_banking_.repository.TransactionRepository;
import com.home_banking_.service.TransactionService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransactionServiceImpl(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Transaction makeTransfer(Long accountOrigenId, Long accountDestinyId, BigDecimal amount, String reason) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new BusinessException("The amount must be greater than zero");
        }

        Account origin = accountRepository.findById(accountOrigenId)
                .orElseThrow(()-> new ResourceNotFoundException("account origin not found"));


        Account destiny = accountRepository.findById(accountDestinyId)
                .orElseThrow(()-> new ResourceNotFoundException("account destiny not found"));


        if (origin.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("Insufficient balance in the source account.");
        }

        //update balance
        origin.setBalance(origin.getBalance().subtract(amount));
        destiny.setBalance(destiny.getBalance().add(amount));

        //save changes
        accountRepository.save(origin);
        accountRepository.save(destiny);

        //register transaction
        Transaction transaction = new Transaction();
        transaction.setAccount_origin(origin);
        transaction.setAccount_destiny(destiny);
        transaction.setAmount(amount);
        transaction.setTypeTransaction(TypeTransaction.DEPOSITO);
        transaction.setStatusTransaction(StatusTransaction.COMPLETED);
        transaction.setCreationDate(LocalDateTime.now());

        return transactionRepository.save(transaction);

    }

    @Override
    public List<Transaction> getTransactionsByAccount(Long accountId) {
        return transactionRepository.findByAccountOrigin_IdOrAccountDestiny_Id(accountId, accountId);
    }


    @Override
    public List<Transaction> getTransactionsByUser(Long userId) {
        return transactionRepository.findByAccountOrigin_User_IdOrAccountDestiny_User_Id(userId, userId);
    }
}
