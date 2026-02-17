package com.home_banking_.dto.RequestDto;

import com.home_banking_.enums.MovementAccountType;
import com.home_banking_.enums.TypeTransaction;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "DTO used to perform a transaction between accounts.")
public class TransactionRequestDto {

    @Schema(description = "Amount of money to be transferred",example = "2500.00")
    @NotBlank(message = "amount is required")
    private BigDecimal amount;

    @Schema(description = "Type of account movement: INCOME or EXPENSE", example = "EXPENSE", implementation = MovementAccountType.class)
    @NotBlank(message = "Movement account is required")
    private MovementAccountType movementAccountType;

    @Schema(description = "Type of transaction to perform: TRANSFER, DEPOSIT, WITHDRAW", example = "TRANSFER", implementation = TypeTransaction.class)
    @NotBlank(message = "Type transaction is required")
    private TypeTransaction typeTransaction;

    @Schema(description = "Reason or description of the transaction", example = "Rent payment for July")
    private String reason;

    @Schema(description = "ID of the destination account (required for transfers)", example = "2002")
    private Long accountDestinyId;

    @Schema(description = "ID of the origin account (required for transfers and withdrawals)", example = "2001")
    private Long accountOriginId;
}
