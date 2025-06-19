package com.home_banking_.model;

import com.home_banking_.enums.MovementAccountType;
import com.home_banking_.enums.StatusTransaction;
import com.home_banking_.enums.TypeTransaction;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private BigDecimal amount;
    private LocalDateTime creationDate;
    private MovementAccountType movementAccount;
    private StatusTransaction statusTransaction;
    private TypeTransaction typeTransaction;

    @ManyToOne
    @JoinColumn(name = "account_origin_id")
    private Account account_origin;

    @ManyToOne
    @JoinColumn(name = "account_destiny")
    private Account account_destiny;

}
