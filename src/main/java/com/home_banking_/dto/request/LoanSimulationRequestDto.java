package com.home_banking_.dto.request;

import com.fasterxml.jackson.databind.annotation.JsonAppend;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanSimulationRequestDto {

    @Schema(description = "Requested loan amount", example = "50000.00", minimum = "1000")
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1000.00", message = "Minimum amount is 1000.00")
    @Digits(integer = 15, fraction = 2, message = "Amount must have up to 2 decimal places")
    private BigDecimal amount;

    @Schema(description = "Number of installments", example = "12")
    @NotNull(message = "Installments are required")
    @Min(value=1, message = "Installments must be at least 1")
    @Max(value = 72, message = "Installments cannot exceed 72")
    private Integer installments;

    @Schema(description = "Account ID requesting the loan", example = "101")
    @NotNull(message = "AccountId is required")
    @Positive(message = "AccountId must be positive")
    private Long accountId;


}
