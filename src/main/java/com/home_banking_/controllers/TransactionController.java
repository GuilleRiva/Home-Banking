package com.home_banking_.controllers;

import com.home_banking_.dto.RequestDto.TransactionRequestDto;
import com.home_banking_.dto.ResponseDto.TransactionResponseDto;
import com.home_banking_.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "Transaction Controller", description = "Operations related to money transfers and account transactions")
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;


    @Operation(
            summary = "Get transactions by account ID",
            description = "Retrieves a list of transactions associated with the specified account."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = TransactionResponseDto.class)))),
            @ApiResponse(responseCode = "404", description = "Account not found or no transactions available")
    })
    @GetMapping("/account/{accountId}")
    @PreAuthorize("hasAnyRole('ADMIN' , 'EMPLOYED')")
    public ResponseEntity<List<TransactionResponseDto>>getTransactionsByAccount(
            @Parameter(name = "accountId", description = "Unique identifier of the account", required = true)
            @PathVariable Long accountId){

        List<TransactionResponseDto> transactions = transactionService.getTransactionsByAccount(accountId);

        return ResponseEntity.ok(transactions);
    }


    @Operation (
            summary = "Get transactions by user ID",
            description = "Returns all transactions performed by the specified user across their accounts."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = TransactionResponseDto.class)))),
            @ApiResponse(responseCode = "404", description = "User not found or no transactions available")
    })
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN' , 'EMPLOYED')")
    public ResponseEntity<List<TransactionResponseDto>>getTransactionsByUser(
            @Parameter(name = "userId", description = "Unique identifier of the user", required = true)
            @PathVariable Long userId){

        List<TransactionResponseDto> transactions = transactionService.getTransactionsByUser(userId);

        return ResponseEntity.ok(transactions);
    }


    @Operation(
            summary = "Make a transfer between accounts",
            description = "Creates a new transaction transferring funds from one account to another."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transfer completed successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TransactionResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid transaction request"),
            @ApiResponse(responseCode = "404", description = "Source or destination account not found")
    })
    @PostMapping
    public ResponseEntity<TransactionResponseDto> makeTransfer(@Valid @RequestBody TransactionRequestDto dto){

        TransactionResponseDto transaction = transactionService.makeTransfer(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

}
