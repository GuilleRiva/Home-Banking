package com.home_banking_.controllers;

import com.home_banking_.dto.RequestDto.LoanRequestDto;
import com.home_banking_.dto.ResponseDto.LoanResponseDto;
import com.home_banking_.exceptions.ResourceNotFoundException;
import com.home_banking_.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @GetMapping("/simulate")
    public ResponseEntity<LoanResponseDto> simulateLoan(@RequestParam @Valid LoanRequestDto dto){

        LoanResponseDto simulated = loanService.simulateLoans(dto);
        return ResponseEntity.ok(simulated);
    }


    @PostMapping("/grant")
    public ResponseEntity<LoanResponseDto> grantLoan(@RequestParam @Valid LoanRequestDto dto){

        LoanResponseDto granted = loanService.grantLoan(dto);
        return new ResponseEntity<>(granted, HttpStatus.CREATED);
    }


    @GetMapping("/account/{accountId}")
    public ResponseEntity<LoanResponseDto> getLoanByAccount(@PathVariable Long accountId){
        return loanService.getLoanByAccount(accountId)
                .map(ResponseEntity::ok)
                .orElseThrow(()-> new ResourceNotFoundException("Loan not found for account ID: " + accountId));
    }

}
