package com.home_banking_.controllers;

import com.home_banking_.dto.RequestDto.AccountRequestDto;
import com.home_banking_.dto.ResponseDto.AccountResponseDto;
import com.home_banking_.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;


@Tag(name= "Account Controller", description = "Bank account management")
@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;


    @Operation(
            summary = "Retrieve all accounts",
            description = "Returns a list containing all bank accounts registered in the system."
    )
    @ApiResponse(responseCode = "200",
            description = "Accounts found",
    content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = AccountResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Accounts not found")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AccountResponseDto>> getAll(){

        List<AccountResponseDto> accounts = accountService.getAll();

        return ResponseEntity.ok(accounts);
    }



    @Operation(
            summary = "Get account by ID",
            description = "Get account information by ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accounts found",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = AccountResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{accountId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AccountResponseDto>getById(
            @Parameter(name = "accountId", description = "Unique identifier of the account", required = true)
            @PathVariable Long accountId){

        AccountResponseDto account = accountService.getAccountById(accountId);

        return ResponseEntity.ok(account);

    }




    @Operation(
            summary = "Obtain account balance",
            description = "Obtain account balances"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account balance found",
            content = @Content(mediaType = "application/json",
            schema = @Schema(type = "number", format = "double", example = "15000.50"))),
            @ApiResponse(responseCode = "404", description = "Accounts not found")

    })
    @GetMapping("/{accountId}/balance")
    public ResponseEntity<BigDecimal> getAccountBalance(
            @Parameter(name = "accountId",description = "Unique identifier of the account", required = true)
            @PathVariable Long accountId){

        BigDecimal balance = accountService.getBalance(accountId);

      return ResponseEntity.ok(balance);

    }



    @Operation(
            summary = "create a new account",
            description = "Creates and returns a new bank account associated with a user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Account successfully created",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = AccountResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping
    public ResponseEntity<AccountResponseDto>createAccount(@RequestBody @Valid AccountRequestDto dto){

        AccountResponseDto created = accountService.createAccount(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);

    }



    @Operation(
            summary = "Get account by alias",
            description = "Search for accounts by an alias"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accounts found",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = AccountResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "The accounts were not found")
    })
    @GetMapping("/alias/{alias}")
    @PreAuthorize("hasRole('ADMIN') or hasRol('EMPLOYED')")
    public ResponseEntity<AccountResponseDto> getAccountByAlias(
            @Parameter(name = "alias", description = "account identifier name", required = true)
            @PathVariable String alias){

        AccountResponseDto account = accountService.getAccountByAlias(alias);

        return ResponseEntity.ok(account);
    }




    @Operation(
            summary = "Delete an account",
            description = "Deletes a bank account permanently by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Account deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @DeleteMapping("/{accountId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAccount(
            @Parameter(name = "accountId", description = "Unique identifier of the account", required = true)
            @PathVariable Long accountId){

        accountService.deleteAccount(accountId);

        return  ResponseEntity.noContent().build();
    }
}
