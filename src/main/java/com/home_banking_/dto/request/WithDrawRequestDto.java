package com.home_banking_.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "DTO used to withdraw money from an account.")
public class WithDrawRequestDto {

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Digits(integer = 15, fraction = 2, message = "Amount myst have up to 2 decimal places")
    @Schema(description = "Amount to withdraw", example = "500.00")
    private BigDecimal amount;

    @NotNull(message = "Account ID is required")
    @Schema(description = "Account ID", example = "2001")
    private Long accountId;

}
