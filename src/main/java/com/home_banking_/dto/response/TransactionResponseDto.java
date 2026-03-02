package com.home_banking_.dto.response;

import com.home_banking_.enums.MovementAccountType;
import com.home_banking_.enums.StatusTransaction;
import com.home_banking_.enums.TransactionOperationType;
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

    private Long id;

    private BigDecimal amount;

    private LocalDateTime creationDate;


    @Schema(description = "Operation type", example = "TRANSFER")
    private TransactionOperationType typeTransaction;

    @Schema(description = "Transaction status", example = "COMPLETED")
    private StatusTransaction statusTransaction;

    @Schema(description = "Destination account id", example = "2003")
    private Long destinationAccountId;

    @Schema(description = "Client-facing reference", example = "b1f2f3c0-7f0e-4d4f-9a3f-2bca6e6d9e10")
    private String reference;
}
