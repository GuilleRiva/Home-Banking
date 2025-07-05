package com.home_banking_.controllers;

import com.home_banking_.dto.RequestDto.TransactionRequestDto;
import com.home_banking_.dto.ResponseDto.TransactionResponseDto;
import com.home_banking_.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;


    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransactionResponseDto>>getTransactionsByAccount(@PathVariable Long accountId){
        List<TransactionResponseDto> transactions = transactionService.getTransactionsByAccount(accountId);

        return ResponseEntity.ok(transactions);
    }



    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TransactionResponseDto>>getTransactionsByUser(@PathVariable Long userId){
        List<TransactionResponseDto> transactions = transactionService.getTransactionsByUser(userId);

        return ResponseEntity.ok(transactions);
    }


    @PostMapping
    public ResponseEntity<TransactionResponseDto> makeTransfer(@Valid @RequestBody TransactionRequestDto dto){

        TransactionResponseDto transaction = transactionService.makeTransfer(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

}
