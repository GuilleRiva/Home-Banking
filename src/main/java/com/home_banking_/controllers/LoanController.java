package com.home_banking_.controllers;

import com.home_banking_.dto.request.LoanGrantRequestDto;
import com.home_banking_.dto.request.LoanRequestDto;
import com.home_banking_.dto.request.LoanSimulationRequestDto;
import com.home_banking_.dto.response.LoanResponseDto;
import com.home_banking_.exceptions.custom.ResourceNotFoundException;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@Slf4j
@Tag(name= "Loan controller", description = "User Loan management")
@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency_Key";


    @Operation(
            summary = "Simulate a loan",
            description = "Calculates the expected repayment terms for a loan based on the input data without granting it."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loan simulation completed successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LoanResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Invalid simulation request")
    })
    @PostMapping("/simulate")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<LoanResponseDto> simulateLoan(@RequestBody @Valid LoanSimulationRequestDto dto){

        LoanResponseDto simulated = loanService.simulateLoans(dto);

        log.info("Loan simulation completed for account ID: {}", dto.getAccountId());
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
    @PostMapping("/{loanId}/grant")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoanResponseDto> grantLoan(
            @RequestHeader(IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
            @PathVariable Long loanId){

        LoanResponseDto response = loanService.grantLoan(idempotencyKey, loanId);

       return ResponseEntity.ok(response);
    }


    @PostMapping("/request")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<LoanResponseDto> requestLoan(
            @RequestHeader(IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
            @Valid @RequestBody LoanRequestDto dto
    ) {
        LoanResponseDto response = loanService.requestLoan(idempotencyKey,dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
    @PreAuthorize("hasAnyRole('ADMIN' , 'EMPLOYED','AUDITOR')")
    public ResponseEntity<LoanResponseDto> getLoanByAccount(
            @Parameter(name = "accountId", description = "ID of the account", required = true)
            @PathVariable Long accountId){

        log.info("GET /api/loans/account/{} - Consulting loan associated with the account", accountId);

        LoanResponseDto loan = loanService.getLoanByAccount(accountId)
                .orElseThrow(()-> new ResourceNotFoundException("Loan not found for account ID:" + accountId));
        return ResponseEntity.ok(loan);
    }

}
