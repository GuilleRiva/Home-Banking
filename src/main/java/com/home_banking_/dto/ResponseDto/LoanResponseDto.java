package com.home_banking_.dto.ResponseDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanResponseDto {

    private Long id;
    private BigDecimal amount;
    private Integer quotas;
    private BigDecimal interestRate;
    private BigDecimal totalToPay;
    private BigDecimal amountQuota;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String statusLoan;
}
