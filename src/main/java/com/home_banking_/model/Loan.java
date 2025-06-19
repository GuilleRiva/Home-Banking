package com.home_banking_.model;

import com.home_banking_.enums.StatusLoan;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String amount;
    private String quotas;
    private BigDecimal interestRate;
    private BigDecimal totalToPay;
    private BigDecimal amountQuota;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    private StatusLoan statusLoan;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

}
