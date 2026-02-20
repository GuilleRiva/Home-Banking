package com.home_banking_.dto.request;

import com.home_banking_.enums.StatusAccount;
import com.home_banking_.enums.TypeAccount;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccountCreateRequestDto {

    @Schema(description = "Alias of the account", example = "mi.cuenta.ahorro")
    @NotBlank(message = "alias is required")
    @Size(min = 6,max = 30, message = "Alias must be between 6 and 30 characters")
    private String alias;


    @Schema(description = "Type of account", example = "CAJA_AHORRO")
    @NotNull(message = "TypeAccount is required")
    private TypeAccount typeAccount;

}
