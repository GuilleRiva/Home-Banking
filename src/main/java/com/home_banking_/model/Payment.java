package com.home_banking_.model;

import com.home_banking_.enums.ServiceEntity;
import com.home_banking_.enums.StatusPayment;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "payment")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String description;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_entity", nullable = false, length = 40)
    private ServiceEntity serviceEntity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_payment", nullable = false, length = 30)
    private StatusPayment statusPayment;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
}
