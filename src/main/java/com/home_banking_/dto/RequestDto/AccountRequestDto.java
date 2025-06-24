package com.home_banking_.dto.RequestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
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
