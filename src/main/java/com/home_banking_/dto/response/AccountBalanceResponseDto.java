package com.home_banking_.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountBalanceResponseDto {
    private Long accountId;
    private BigDecimal balance;
    private LocalDateTime asOf;
}
