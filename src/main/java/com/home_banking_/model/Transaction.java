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

    @Enumerated(EnumType.STRING)
    private MovementAccountType movementAccountType;

    @Enumerated(EnumType.STRING)
    private StatusTransaction statusTransaction;

    @Enumerated(EnumType.STRING)
    private TypeTransaction typeTransaction;

    @ManyToOne
    @JoinColumn(name = "account_origin_id")
    private Account accountOrigin;

    @ManyToOne
    @JoinColumn(name = "account_destiny_id")
    private Account accountDestiny;

}
