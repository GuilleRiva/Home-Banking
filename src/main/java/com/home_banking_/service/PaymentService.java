package com.home_banking_.service;

import com.home_banking_.dto.RequestDto.PaymentRequestDto;
import com.home_banking_.dto.ResponseDto.PaymentResponseDto;
import com.home_banking_.enums.ServiceEntity;
import com.home_banking_.model.Payment;

import java.util.List;

public interface PaymentService {

    PaymentResponseDto makePayment(PaymentRequestDto dto);

    List<PaymentResponseDto> getPaymentByAccount (Long accountId);

    List<PaymentResponseDto> getPaymentByEntity(ServiceEntity entity);
}
