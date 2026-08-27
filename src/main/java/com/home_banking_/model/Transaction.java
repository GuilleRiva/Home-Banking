package com.home_banking_.model;

import com.home_banking_.enums.Currency;
import com.home_banking_.enums.MovementAccountType;
import com.home_banking_.enums.StatusTransaction;
import com.home_banking_.enums.TransactionOperationType;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate;

    @Column(length = 255)
    private String description;

    @Column(nullable = false, unique = true, length = 100)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_account_type", nullable = false, length = 30)
    private MovementAccountType movementAccountType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_transaction", nullable = false, length = 30)
    private StatusTransaction statusTransaction;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_transaction", nullable = false, length = 30)
    private TransactionOperationType typeTransaction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Currency currency;


    @ManyToOne
    @JoinColumn(name = "account_origin_id")
    private Account accountOrigin;

    @ManyToOne
    @JoinColumn(name = "account_destiny_id")
    private Account accountDestiny;

}
