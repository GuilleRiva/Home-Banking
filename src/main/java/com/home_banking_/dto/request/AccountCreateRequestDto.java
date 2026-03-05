package com.home_banking_.dto.request;

import com.home_banking_.enums.TypeAccount;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AccountCreateRequestDto {

    @Schema(description = "Alias of the account", example = "mi.cuenta.ahorro",minLength = 6, maxLength = 30)
    @NotBlank(message = "alias is required")
    @Size(min = 6,max = 30, message = "Alias must be between 6 and 30 characters")
    @Pattern(
            regexp = "^[a-z0-9]+([.-][a-z0-9]+)*$",
            message = "Alias must contain only lowercase letters, numbers, dots or hyphens, and cannot start/end with a separator"
    )
    private String alias;


    @Schema(description = "Type of account",example = "CAJA_AHORRO", implementation = TypeAccount.class)
    @NotNull(message = "TypeAccount is required")
    private TypeAccount typeAccount;

}
