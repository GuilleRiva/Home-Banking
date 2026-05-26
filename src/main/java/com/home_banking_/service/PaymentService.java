package com.home_banking_.service;

import com.home_banking_.dto.request.PaymentRequestDto;
import com.home_banking_.dto.request.ServicePaymentRequestDto;
import com.home_banking_.dto.response.PaymentResponseDto;
import com.home_banking_.enums.ServiceEntity;

import java.util.List;

public interface PaymentService {

    PaymentResponseDto makePayment(String idempotencyKey, PaymentRequestDto dto) ;

    PaymentResponseDto payService(String idempotencyKey, ServicePaymentRequestDto dto);

    List<PaymentResponseDto> getPaymentByAccount (Long accountId);

    List<PaymentResponseDto> getPaymentByEntity(ServiceEntity entity);
}
