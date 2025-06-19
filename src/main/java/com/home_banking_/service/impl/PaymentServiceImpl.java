package com.home_banking_.service.impl;

import com.home_banking_.enums.ServiceEntity;
import com.home_banking_.enums.StatusPayment;
import com.home_banking_.exceptions.BusinessException;
import com.home_banking_.exceptions.ResourceNotFoundException;
import com.home_banking_.model.Account;
import com.home_banking_.model.Payment;
import com.home_banking_.repository.AccountRepository;
import com.home_banking_.repository.PaymentRepository;
import com.home_banking_.service.PaymentService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository, AccountRepository accountRepository) {
        this.paymentRepository = paymentRepository;
        this.accountRepository = accountRepository;
    }


    @Override
    public Payment makePayment(Long accountId, BigDecimal amount, ServiceEntity serviceEntity, String description) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(()-> new ResourceNotFoundException("Account not found"));

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("The amount must be greater than zero.");
        }

        if (account.getBalance().compareTo(amount) < 0){
            throw new RuntimeException("Insufficient balance to make payment.");
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        Payment payment = new Payment();
        payment.setAccount(account);
        payment.setServiceEntity(serviceEntity);
        payment.setDescription(description);
        payment.setAmount(amount);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatusPayment(StatusPayment.PROCESSING);

        return paymentRepository.save(payment);
    }


    @Override
    public List<Payment> getPaymentByAccount(Long accountId) {
        return paymentRepository.findByAccount(accountId);
    }

    @Override
    public List<Payment> getPaymentByEntity(ServiceEntity entity) {
        return paymentRepository.findByServiceEntity(entity);
    }
}
