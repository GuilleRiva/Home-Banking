package com.home_banking_.model;

import com.home_banking_.enums.Currency;
import com.home_banking_.enums.LoanStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Getter
@Setter
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
    @Column(name = "statusLoan", nullable = false)
    private LoanStatus loanStatus;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

}
