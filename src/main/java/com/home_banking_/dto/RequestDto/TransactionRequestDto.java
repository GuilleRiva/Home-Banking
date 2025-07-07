package com.home_banking_.dto.RequestDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionRequestDto {

    @NotBlank(message = "amount is required")
    private BigDecimal amount;

    @NotBlank(message = "Movement account is required")
    private String movementAccountType;

    @NotBlank(message = "Type transaction is required")
    private String typeTransaction;

    private String reason;

    private Long accountDestinyId;

    private Long accountOriginId;
}
