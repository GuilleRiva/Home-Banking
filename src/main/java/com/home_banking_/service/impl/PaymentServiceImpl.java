package com.home_banking_.service.impl;

import com.home_banking_.dto.RequestDto.PaymentRequestDto;
import com.home_banking_.dto.ResponseDto.PaymentResponseDto;
import com.home_banking_.enums.ServiceEntity;
import com.home_banking_.enums.StatusPayment;
import com.home_banking_.exceptions.ResourceNotFoundException;
import com.home_banking_.mappers.PaymentMapper;
import com.home_banking_.model.Account;
import com.home_banking_.model.Payment;
import com.home_banking_.repository.AccountRepository;
import com.home_banking_.repository.PaymentRepository;
import com.home_banking_.service.PaymentService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;
    private final PaymentMapper paymentMapper;

    public PaymentServiceImpl(PaymentRepository paymentRepository, AccountRepository accountRepository, PaymentMapper paymentMapper) {
        this.paymentRepository = paymentRepository;
        this.accountRepository = accountRepository;
        this.paymentMapper = paymentMapper;
    }


    @Override
    public PaymentResponseDto makePayment(PaymentRequestDto dto) {
        Account account = accountRepository.findById(dto.getAccountId())
                .orElseThrow(()-> new ResourceNotFoundException("Account not found"));




        Payment payment = paymentMapper.toEntity(dto);
        payment.setDescription(dto.getDescription());
        payment.setAccount(account);
        payment.setAmount(dto.getAmount());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatusPayment(StatusPayment.COMPLETED);
        payment.setServiceEntity(ServiceEntity.TARJETA_CREDITO);
        paymentRepository.save(payment);

        return paymentMapper.toDto(payment);
    }

    @Override
    public List<PaymentResponseDto> getPaymentByAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(()-> new ResourceNotFoundException("Account not found"));

        List<Payment> payments = paymentRepository.findByAccount_Id(accountId);

        return payments.stream()
                .map(paymentMapper::toDto)
                .collect(Collectors.toList());
    }



    @Override
    public List<PaymentResponseDto> getPaymentByEntity(ServiceEntity entity) {
        List<Payment> payments = paymentRepository.findByServiceEntity(entity);

        return payments.stream()
                .map(paymentMapper::toDto)
                .collect(Collectors.toList());
    }
}
