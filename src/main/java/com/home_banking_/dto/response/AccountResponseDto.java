package com.home_banking_.dto.ResponseDto;

import com.home_banking_.enums.StatusAccount;
import com.home_banking_.enums.TypeAccount;
import com.home_banking_.model.Account;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountResponseDto {

    @Schema(description = "Unique account ID", example = "200")
    private Long id;

    @Schema(description = "Account number", example = "123456789")
    private String accountNumber;

    @Schema(description = "CBU of the account", example = "0123456789012345678901")
    private String CBU;

    @Schema(description = "Current account balance", example = "15000.75")
    private BigDecimal balance;

    private String alias;

    @Schema(description = "Type of the account", example = "CAJA_AHORRO",
    implementation = TypeAccount.class)
    private TypeAccount typeAccount;

    @Schema(description = "Status of the account", example = "ACTIVE",
    implementation = StatusAccount.class)
    private StatusAccount statusAccount;

    @Schema(description = "Creation date of the account", example = "2025-07-08T10:00:00")
    private LocalDateTime creationDate;

}
