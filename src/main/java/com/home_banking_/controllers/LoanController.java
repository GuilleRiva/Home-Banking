package com.home_banking_.controllers;

import com.home_banking_.model.Loan;
import com.home_banking_.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @GetMapping("/simulate")
    public ResponseEntity<Loan> simulateLoan(@RequestParam BigDecimal amount,
                                             @RequestParam int quotas,
                                             @RequestParam BigDecimal interestRate){

        Loan simulation = loanService.simulateLoans(amount, quotas, interestRate);
        return ResponseEntity.ok(simulation);
    }


    @PostMapping("/grant")
    public ResponseEntity<Loan> grantLoan(@RequestParam Long accountId,
                                          @RequestParam BigDecimal amount,
                                          @RequestParam int quotas,
                                           @RequestParam BigDecimal interestRate){

        Loan loan = loanService.grantLoan(accountId, amount, quotas, interestRate);
        return ResponseEntity.status(HttpStatus.CREATED).body(loan);
    }


    @GetMapping("/account/{accountId}")
    public ResponseEntity<Loan> getLoanByAccount(@PathVariable Long accountId){
        return loanService.getLoanByAccount(accountId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
