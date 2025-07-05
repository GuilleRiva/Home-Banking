package com.home_banking_.controllers;

import com.home_banking_.dto.RequestDto.AccountRequestDto;
import com.home_banking_.dto.ResponseDto.AccountResponseDto;
import com.home_banking_.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<List<AccountResponseDto>> getAll(){
        List<AccountResponseDto> accounts = accountService.getAll();

        return ResponseEntity.ok(accounts);
    }


    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponseDto>getById(@PathVariable Long accountId){
        AccountResponseDto account = accountService.getAccountById(accountId);

        return ResponseEntity.ok(account);

    }




    @GetMapping("/{accountId}/balance")
    public ResponseEntity<BigDecimal> getAccountBalance(@PathVariable Long accountId){
        BigDecimal balance = accountService.getBalance(accountId);

      return ResponseEntity.ok(balance);

    }


    @PostMapping("/account")
    public ResponseEntity<AccountResponseDto>createAccount(@RequestBody @Valid AccountRequestDto dto){
        AccountResponseDto created = accountService.createAccount(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);

    }

    @GetMapping("/alias/{alias}")
    public ResponseEntity<AccountResponseDto> getAccountByAlias(@PathVariable String alias){
        AccountResponseDto account = accountService.getAccountByAlias(alias);
        return ResponseEntity.ok(account);
    }


    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long accountId){
        accountService.deleteAccount(accountId);
        return  ResponseEntity.noContent().build();
    }
}
