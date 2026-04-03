package com.home_banking_.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Schema(description = "DTO used for functional banking operations")
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDepositRequestDto {
    @NotNull
    @Schema(description = "Account ID is required")
    private Long accountId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Digits(integer = 15, fraction = 2, message = "Amount must have up to 2 decimal places")
    @Schema(description = "Amount to deposit", example = "1500.00")
    private BigDecimal amount;

    @NotBlank
    @Size(max = 255, message = "Message must not exceed 255 characters")
    private String description;

}
