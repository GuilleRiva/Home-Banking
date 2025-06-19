package com.home_banking_.service;

import com.home_banking_.model.Transaction;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionService {

    Transaction makeTransfer (Long accountOrigenId, Long accountDestinyId, BigDecimal amount, String reason);

    List<Transaction> getTransactionsByAccount(Long accountId);

    List<Transaction> getTransactionsByUser(Long userId);
}
