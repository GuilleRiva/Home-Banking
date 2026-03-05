package com.home_banking_.dto.response;

import com.home_banking_.enums.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO representing the balance of an account at a specific moment.")
public class AccountBalanceResponseDto {

    @Schema(description = "Unique identifier of the account", example = "15")
    private Long accountId;

    @Schema(description = "Current balance of the account", example = "1500.75")
    private BigDecimal balance;

    @Schema(description = "Timestamp indicating when the balance was retrieved", example = "2025-07-08T14:35:00")
    private LocalDateTime asOf;

    @Schema(description = "Type of currency", example = "ARS", implementation = Currency.class)
    private Currency currency;
}
