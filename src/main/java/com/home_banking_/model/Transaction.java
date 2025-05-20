package com.home_banking_.model;

import com.home_banking_.enums.MovementAccountType;
import com.home_banking_.enums.StatusTransaction;
import com.home_banking_.enums.TypeTransaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {
    private Long id;
    private BigDecimal amount;
    private LocalDateTime creationDate;
    private MovementAccountType movementAccount;
    private StatusTransaction statusTransaction;
    private TypeTransaction typeTransaction;

}
