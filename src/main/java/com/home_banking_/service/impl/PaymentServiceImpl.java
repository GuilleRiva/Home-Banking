package com.home_banking_.service.impl;

import com.home_banking_.dto.request.PaymentRequestDto;
import com.home_banking_.dto.response.PaymentResponseDto;
import com.home_banking_.enums.ServiceEntity;
import com.home_banking_.enums.StatusPayment;
import com.home_banking_.exceptions.ResourceNotFoundException;
import com.home_banking_.mappers.PaymentMapper;
import com.home_banking_.model.Account;
import com.home_banking_.model.Payment;
import com.home_banking_.repository.AccountRepository;
import com.home_banking_.repository.PaymentRepository;
import com.home_banking_.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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

    @Transactional
    @Override
    public PaymentResponseDto makePayment(PaymentRequestDto dto) {

        Account account = accountRepository.findById(dto.getAccountId())
                .orElseThrow(()-> new ResourceNotFoundException(
                        "Account not found"
                ));


        Payment payment = paymentMapper.toEntity(dto);
        payment.setDescription(dto.getDescription());
        payment.setAccount(account);
        payment.setAmount(dto.getAmount());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatusPayment(StatusPayment.COMPLETED);
        payment.setServiceEntity(ServiceEntity.TARJETA_CREDITO);

        paymentRepository.save(payment);

        log.info("Payment successfully registered accountId: {} | PaymentId: {} | Amount: {}",
                dto.getAccountId(), payment.getId(), dto.getAmount());

        return paymentMapper.toDto(payment);
    }

    @Transactional(readOnly = true)
    @Override
    public List<PaymentResponseDto> getPaymentByAccount(Long accountId) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(()->  new ResourceNotFoundException(
                        "Account not found"
                ));

        List<Payment> payments = paymentRepository.findByAccount_Id(accountId);

        log.info("Total payments found for account ID {}: {} ",accountId, payments.size());

        return payments.stream()
                .map(paymentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<PaymentResponseDto> getPaymentByEntity(ServiceEntity entity) {

        List<Payment> payments = paymentRepository.findByServiceEntity(entity);

        log.info("Total payments found for entity {}: {}", entity, payments.size());

        return payments.stream()
                .map(paymentMapper::toDto)
                .collect(Collectors.toList());
    }
}
