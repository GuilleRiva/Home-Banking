package com.home_banking_.model;

import com.home_banking_.enums.LoanStatus;
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
    private BigDecimal amount;
    private Integer installments;
    private BigDecimal interestRate;
    private BigDecimal totalToPay;
    private BigDecimal InstallmentsAmount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    private LoanStatus statusLoan;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

}
