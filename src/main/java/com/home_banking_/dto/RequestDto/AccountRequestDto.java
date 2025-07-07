package com.home_banking_.dto.RequestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccountRequestDto {

    private String accountNumber;

    @NotBlank(message = "alias is required")
    private String alias;

    private String CBU;

    @NotNull
    private String typeAccount;

    @NotNull
    private String statusAccount;

    @NotNull
    private Long userId;

}
