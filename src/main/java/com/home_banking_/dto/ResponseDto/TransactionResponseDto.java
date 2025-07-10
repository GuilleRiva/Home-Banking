package com.home_banking_.dto.ResponseDto;

import com.home_banking_.enums.MovementAccountType;
import com.home_banking_.enums.StatusPayment;
import com.home_banking_.enums.StatusTransaction;
import com.home_banking_.enums.TypeTransaction;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponseDto {

    @Schema(description = "Unique identifier", example = "52")
    private Long id;

    @Schema(description = "Amount of money to be transferred",example = "2500.00")
    private BigDecimal amount;

    @Schema(description = "date the transaction was made",example = "2025-07-08T14:35:00")
    private LocalDateTime creationDate;

    @Schema(description = "Type of account movement: INCOME or EXPENSE", example = "EXPENSE", implementation = MovementAccountType.class)
    private MovementAccountType movementAccountType;

    @Schema(description = "Type of transaction to perform: TRANSFER, DEPOSIT, WITHDRAW", example = "TRANSFER", implementation = TypeTransaction.class)
    private TypeTransaction typeTransaction;

    @Schema(description = "Status transaction", example = "COMPLETED", implementation = StatusTransaction.class)
    private StatusTransaction statusTransaction;
}
