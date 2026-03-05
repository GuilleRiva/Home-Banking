package com.home_banking_.dto.response;

import com.home_banking_.enums.Currency;
import com.home_banking_.enums.TypeAccount;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO representing a summarized view of an account")
public class AccountSummaryDto {

    @Schema(description = "Unique identifier of the account", example = "15")
    private Long id;

    @Schema(description = "Alias of the account", example = "mi.cuenta.ahorro")
    private String alias;

    @Schema(description = "Current balance of the account",example = "1582.00")
    private BigDecimal balance;

    @Schema(description = "Type of the account", implementation = TypeAccount.class)
    private TypeAccount typeAccount;

    @Schema(description = "Type of currency", example = "ARS")
    private Currency currency;
}
