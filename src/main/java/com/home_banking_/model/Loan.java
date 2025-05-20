package com.home_banking_.model;

import com.home_banking_.enums.StatusLoan;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Loan {
    private Long id;
    private String amount;
    private String quotas;
    private BigDecimal interestRate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private StatusLoan statusLoan;

    private Account account;

}
