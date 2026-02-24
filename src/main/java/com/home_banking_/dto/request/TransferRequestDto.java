package com.home_banking_.dto.request;

import com.home_banking_.enums.TransactionOperationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "DTO used to perform a transaction between accounts.")
public class TransferRequestDto {

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Digits(integer = 15, fraction = 2, message = "Amount must have up to 2 decimal places")
    @Schema(description = "Amount to be processed", example = "2500.00")
    private BigDecimal amount;


    @NotNull(message = "Destination account ID is required")
    @Schema(description = "Destination account ID", example = "2002")
    private Long destinationAccountId;

    @NotNull(message = "Origin account ID is required")
    @Schema(description = "Origin account ID", example = "2001")
    private Long originAccountId;

}
