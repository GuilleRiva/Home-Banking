package com.home_banking_.dto.response;

import com.home_banking_.enums.StatusAccount;
import com.home_banking_.enums.TypeAccount;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO representing account details with sensitive data masked")
public class AccountResponseDto {

    @Schema(description = "Unique identifier of the account", example = "15")
    private Long id;

    @Schema(description = "Account number (masked)", example = "****6789")
    private String accountNumberMasked;

    @Schema(description = "CBU (masked)", example = "**********8901")
    private String cbuMasked;

    @Schema(description = "Alias of the account", example = "mi.cuenta.ahorro")
    private String alias;

    @Schema(implementation = StatusAccount.class)
    private StatusAccount statusAccount;

    @Schema(implementation = TypeAccount.class)
    private TypeAccount typeAccount;

    @Schema(description = "Date when the account was created", example = "2025-07-08T14:35:00")
    private LocalDateTime creationDate;

}
