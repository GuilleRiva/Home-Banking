package com.home_banking_.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanGrantRequestDto {

    @NotNull(message = "AccountId is required")
    @Positive(message = "AccountId must be positive")
    private Long accountId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1000.00")
    @Digits(integer = 15, fraction = 2)
    private BigDecimal amount;

    @NotNull(message = "Installments are required")
    @Min(1)
    @Max(72)
    private Integer installments;
}
