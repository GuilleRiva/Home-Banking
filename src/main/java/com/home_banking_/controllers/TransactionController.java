package com.home_banking_.controllers;

import com.home_banking_.model.Transaction;
import com.home_banking_.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;


    @GetMapping("/account/{accountId/transaction")
    public ResponseEntity<List<Transaction>>getTransactionsByAccount(@PathVariable Long accountId){
        List<Transaction> transactions = transactionService.getTransactionsByAccount(accountId);

        return ResponseEntity.ok(transactions);
    }



    @GetMapping("/user/{userId}/transaction")
    public ResponseEntity<List<Transaction>>getTransactionsByUser(@PathVariable Long userId){
        List<Transaction> transactions = transactionService.getTransactionsByUser(userId);

        return ResponseEntity.ok(transactions);
    }


    @PostMapping
    public ResponseEntity<Transaction> makeTransfer(@RequestParam Long accountOriginId,
                                                    @RequestParam Long accountDestinyId,
                                                    @RequestParam BigDecimal amount,
                                                    @RequestParam String reason){

        Transaction transaction = transactionService.makeTransfer(accountOriginId, accountDestinyId, amount, reason);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

}
