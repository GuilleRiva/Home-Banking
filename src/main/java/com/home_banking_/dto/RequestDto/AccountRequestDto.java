package com.home_banking_.dto.RequestDto;

import com.home_banking_.enums.StatusAccount;
import com.home_banking_.enums.TypeAccount;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccountRequestDto {

    private String accountNumber;

    @Schema(description = "Alias of the account", example = "mi.cuenta.ahorro")
    @NotBlank(message = "alias is required")
    private String alias;

    private String CBU;

    @Schema(description = "Type of account", example = "CAJA?AHORRO",implementation = TypeAccount.class)
    @NotNull
    private TypeAccount typeAccount;

    @Schema(description = "Status of account", example = "Active or Expired", implementation = StatusAccount.class)
    @NotNull
    private StatusAccount statusAccount;

    @Schema(description = "ID of the user who owns th account", example = "1001")
    @NotNull
    private Long userId;

}
