package com.home_banking_.dto.RequestDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanRequestDto {

    @NotBlank(message = "amount is required")
    private BigDecimal amount;

    @NotBlank(message = "quotas are required ")
    private Integer quotas;

}
