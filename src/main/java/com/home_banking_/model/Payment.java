package com.home_banking_.model;

import com.home_banking_.enums.ServiceEntity;
import com.home_banking_.enums.StatusPayment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {
    private Long id;
    private String description;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private ServiceEntity serviceEntity;
    private StatusPayment statusPayment;
}
