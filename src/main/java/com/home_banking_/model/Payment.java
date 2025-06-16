package com.home_banking_.model;

import com.home_banking_.enums.ServiceEntity;
import com.home_banking_.enums.StatusPayment;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private ServiceEntity serviceEntity;
    private StatusPayment statusPayment;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;
}
