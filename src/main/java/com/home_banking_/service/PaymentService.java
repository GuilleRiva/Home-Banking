package com.home_banking_.service;

import com.home_banking_.enums.ServiceEntity;
import com.home_banking_.model.Payment;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {

    Payment makePayment(Long accountId, BigDecimal amount, ServiceEntity serviceEntity, String description);

    List<Payment> getPaymentByAccount (Long accountId);

    List<Payment> getPaymentByEntity(ServiceEntity entity);
}
