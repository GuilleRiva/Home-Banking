package com.home_banking_.controllers;

import com.home_banking_.dto.RequestDto.LoanRequestDto;
import com.home_banking_.dto.ResponseDto.LoanResponseDto;
import com.home_banking_.exceptions.ResourceNotFoundException;
import com.home_banking_.service.LoanService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.ranges.RangeException;


@Tag(name= "Loan controller", description = "User Loan management")
@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;


    @Operation(
            summary = "Simulate a loan",
            description = "Calculates the expected repayment terms for a loan based on the input data without granting it."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loan simulation completed successfully",
            content = @Content(mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = LoanResponseDto.class)))),
            @ApiResponse(responseCode = "404", description = "Invalid simulation request")
    })
    @PostMapping("/simulate")
    public ResponseEntity<LoanResponseDto> simulateLoan(@RequestBody @Valid LoanRequestDto dto){

        LoanResponseDto simulated = loanService.simulateLoans(dto);
        return ResponseEntity.ok(simulated);
    }



    @Operation(
            summary = "Grant a loan",
            description = "Processes and approves a loan request based on the submitted data. Returns the loan " +
                    "details if granted successfully."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Loan granted successfully.",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = LoanResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid loan request")
    })
    @PostMapping("/grant")
    public ResponseEntity<LoanResponseDto> grantLoan(@RequestBody @Valid LoanRequestDto dto){

        LoanResponseDto granted = loanService.grantLoan(dto);
        return new ResponseEntity<>(granted, HttpStatus.CREATED);
    }



    @Operation(
            summary = "Get loan by account ID",
            description = "Retrieves the loan information associated with the specified bank account ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loan retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = LoanResponseDto.class))))
    })
    @GetMapping("/account/{accountId}")
    public ResponseEntity<LoanResponseDto> getLoanByAccount(
            @Parameter(name = "accountId", description = "ID of the account", required = true)
            @PathVariable Long accountId){

        return loanService.getLoanByAccount(accountId)
                .map(ResponseEntity::ok)
                .orElseThrow(()-> new ResourceNotFoundException("Loan not found for account ID: " + accountId));
    }

}
