package com.home_banking_.controllers;

import com.home_banking_.model.Account;
import com.home_banking_.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
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
    public ResponseEntity<List<Account>> getAll(){
        List<Account> accounts = accountService.getAll();

        return ResponseEntity.ok(accounts);
    }


    @GetMapping("/{accountId}")
    public ResponseEntity<Account>getById(@PathVariable Long accountId){
        Account account = accountService.getAccountById(accountId);

        return ResponseEntity.ok(account);

    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Account>> getAccountByUserId(@PathVariable Long userId){
        List<Account> accounts = accountService.getAccountByUserId(userId);
        return ResponseEntity.ok(accounts);
    }


    @GetMapping("/{accountId}/balance")
    public ResponseEntity<BigDecimal> getAccountBalance(@PathVariable Long accountId){
        BigDecimal balance = accountService.getBalance(accountId);

      return ResponseEntity.ok(balance);

    }


    @PostMapping
    public ResponseEntity<Account>createAccount(@RequestBody Account newAccount, Long accountId){
        Account created = accountService.createAccount(newAccount, accountId);
        return new ResponseEntity<>(created, HttpStatus.CREATED);

    }

    @GetMapping("/alias/{alias}")
    public ResponseEntity<Account>getAccountByAlias(@PathVariable String alias){
        Account account = accountService.getAccountByAlias(alias);
        return ResponseEntity.ok(account);
    }


    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long accountId){
        accountService.deleteAccount(accountId);
        return  ResponseEntity.noContent().build();
    }
}
