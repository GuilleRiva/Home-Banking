package com.home_banking_.dto.response;

import com.home_banking_.enums.StatusAccount;
import com.home_banking_.enums.TypeAccount;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountResponseDto {

    private Long id;

    @Schema(description = "Account number (masked)", example = "****6789")
    private String accountNumberMasked;

    @Schema(description = "CBU (masked)", example = "**********8901")
    private String cbuMasked;

    private String alias;

    private StatusAccount statusAccount;
    private TypeAccount typeAccount;

    private LocalDateTime creationDate;

}
