package com.home_banking_.dto.response;

import com.home_banking_.enums.Currency;
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
@Schema(description = "DTO representing a financial transaction")
public class TransactionResponseDto {

    @Schema(description = "Unique identifier of the transaction", example = "5001")
    private Long id;

    @Schema(description = "Transaction amount", example = "5962.00")
    private BigDecimal amount;

    @Schema(description = "Date and time when the transaction was created", example = "2025-07-08T14:35:00")
    private LocalDateTime createdAt;

    @Schema(description = "Currency of the transaction", example = "USD")
    private Currency currency;

    @Schema(description = "Operation type", example = "TRANSFER")
    private TransactionOperationType typeTransaction;

    @Schema(description = "Transaction status", example = "COMPLETED")
    private StatusTransaction statusTransaction;

    @Schema(description = "Destination account id", example = "2003")
    private Long destinationAccountId;

    @Schema(description = "Origin account identifier", example = "12235")
    private Long originAccountId;

    @Schema(description = "Client-facing reference", example = "b1f2f3c0-7f0e-4d4f-9a3f-2bca6e6d9e10")
    private String reference;
}
