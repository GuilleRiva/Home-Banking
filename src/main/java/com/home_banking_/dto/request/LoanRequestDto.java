package com.home_banking_.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanRequestDto {

    private Long accountId;
    private BigDecimal amount;
    private Integer installments;
}
