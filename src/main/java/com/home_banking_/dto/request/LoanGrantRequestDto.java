package com.home_banking_.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanGrantRequestDto {

    @NotNull(message = "loanId is required")
    @Positive(message = "loanId must be positive")
    private Long loanId;

}
