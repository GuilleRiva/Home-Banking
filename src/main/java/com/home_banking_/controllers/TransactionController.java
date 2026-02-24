package com.home_banking_.controllers;

import com.home_banking_.dto.request.DepositRequestDto;
import com.home_banking_.dto.request.TransferRequestDto;
import com.home_banking_.dto.request.WithDrawRequestDto;
import com.home_banking_.dto.response.TransactionResponseDto;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "Transaction", description = "Operations related to account transactions and money movement")
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Validated
public class TransactionController {

    private final TransactionService transactionService;

    //--------------------------------
    // Customer endpoints (JWT user)
    // ------------------------------

    @Operation(
            summary = "Get my transactions",
            description = "Returns all transactions for the authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = TransactionResponseDto.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN' , 'EMPLOYED', 'CLIENT')")
    public ResponseEntity<List<TransactionResponseDto>>getMyTransactions(){
        return ResponseEntity.ok(transactionService.getMyTransactions());
    }

    @Operation (
            summary = "Get my transactions by account",
            description = "Returns transactions for one of the authenticated user's accounts."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = TransactionResponseDto.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("/me/accounts/{accountId}")
    @PreAuthorize("hasAnyRole('ADMIN' , 'EMPLOYED', 'CLIENT')")
    public ResponseEntity<List<TransactionResponseDto>>getMyTransactionsByAccount(
            @Parameter(name = "Account ID", required = true)
            @PathVariable Long accountId){

        return ResponseEntity.ok(transactionService.getMyTransactionsByAccount(accountId));
    }

    // ----------------------------------
    // Operations (create tx)
    // ---------------------------------

    @Operation(
            summary = "Make a transfer between accounts",
            description = "Create a transfer transaction form an origin account to a destination account."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transfer completed successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TransactionResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN', 'EMPLOYED')")
    public ResponseEntity<TransactionResponseDto> makeTransfer(@Valid @RequestBody TransferRequestDto dto){

        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.makeTransfer(dto));
    }

    @Operation(summary = "Deposit money into an account",
    description = "Creates a deposit transaction.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "2010", description = "Deposit completed successfully",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = TransactionResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PostMapping("/deposit")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN', 'EMPLOYED')")
    public ResponseEntity<TransactionResponseDto> deposit (@Valid @RequestBody DepositRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.makeDeposit(dto));
    }

    @Operation(summary = "Withdraw money from an account",
    description = "Creates a withdraw transaction.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Withdraw completed successfully",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = TransactionResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PostMapping("/withdraw")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN','EMPLOYED')")
    public ResponseEntity<TransactionResponseDto> withdraw(@Valid @RequestBody WithDrawRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.makeWithdraw(dto));
    }

    //-----------------------------
    // Admin/ Backoffice endpoints
    //------------------------------

    @Operation(summary = "Get transactions by account ID (admin)",
    description = "Returns transactions for a given account. Admin/backoffice usage.")
    @GetMapping("/accounts/{accountId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYED')")
    public ResponseEntity<List<TransactionResponseDto>> getTransactionsByAccount(@PathVariable Long accountId) {
        return ResponseEntity.ok(transactionService.getTransactionsByAccount(accountId));
    }

    @Operation(summary = "Get transaction by user ID (admin)",
    description = "Returns transactions for a given user. Admin/backoffice usage.")
    @GetMapping("/users/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYED')")
    public ResponseEntity<List<TransactionResponseDto>> getTransactionByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(transactionService.getTransactionsByUser(userId));
    }

}
