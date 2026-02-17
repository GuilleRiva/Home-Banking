package com.home_banking_.dto.ResponseDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountSummaryDto {
    private Long id;
    private String alias;
    private BigDecimal balance;
}
