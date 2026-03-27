package com.home_banking_.service.impl;

import com.home_banking_.dto.request.PaymentRequestDto;
import com.home_banking_.dto.response.PaymentResponseDto;
import com.home_banking_.enums.*;
import com.home_banking_.exceptions.BusinessException;
import com.home_banking_.exceptions.ResourceNotFoundException;
import com.home_banking_.mappers.PaymentMapper;
import com.home_banking_.model.Account;
import com.home_banking_.model.Payment;
import com.home_banking_.repository.AccountRepository;
import com.home_banking_.repository.PaymentRepository;
import com.home_banking_.service.AuditLogService;
import com.home_banking_.service.PaymentService;
import com.home_banking_.service.security.CurrentUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;
    private final PaymentMapper paymentMapper;
    private final CurrentUserService currentUserService;
    private final AuditLogService auditLogService;

    public PaymentServiceImpl(PaymentRepository paymentRepository, AccountRepository accountRepository, PaymentMapper paymentMapper, CurrentUserService currentUserService, AuditLogService auditLogService) {
        this.paymentRepository = paymentRepository;
        this.accountRepository = accountRepository;
        this.paymentMapper = paymentMapper;
        this.currentUserService = currentUserService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    @Override
    public PaymentResponseDto makePayment(PaymentRequestDto dto) {
        String email = currentUserService.getCurrentUserEmail();

        log.info("[PAYMENT_INIT] userEmail={} accountId={} amount={}",
                email, dto.getAccountId(), dto.getAmount());

        Account account = getOwnedAccount(dto.getAccountId(), email);

        validateAccountActive(account);
        validateAmount(dto.getAmount());
        validateSufficientBalance(account, dto.getAmount());

        account.setBalance(account.getBalance().subtract(dto.getAmount()));

        Payment payment = paymentMapper.toEntity(dto);
        payment.setDescription(dto.getDescription());
        payment.setAccount(account);
        payment.setAmount(dto.getAmount());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatusPayment(StatusPayment.COMPLETED);

        Payment savedPayment = paymentRepository.save(payment);
        accountRepository.save(account);

       log.info("[PAYMENT_SUCCESS] userEmail={} accountId={} paymentId={} amount={}",
               email, account.getId(),savedPayment.getId(), savedPayment.getAmount());

       auditLogService.registerPaymentEvent(
               account.getUsers().getId(),
               "Payment completed. accountId=" + account.getId() + ", amount=" + dto.getAmount(),
               AuditType.TRANSACTION,
               PaymentAuditAction.PAYMENT_COMPLETED
       );

        return paymentMapper.toDto(savedPayment);
    }

    @Transactional(readOnly = true)
    @Override
    public List<PaymentResponseDto> getPaymentByAccount(Long accountId) {
        String email = currentUserService.getCurrentUserEmail();
        Account account = getOwnedAccount(accountId, email);

        List<Payment> payments = paymentRepository.findByAccount_Id(accountId);

       log.info("[FETCH_PAYMENTS_BY_ACCOUNT_SUCCESS] userEmail={} accountId={} totalToPayments={}",
               email, accountId, payments.size());

        return payments.stream()
                .map(paymentMapper::toDto)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYED')")
    @Transactional(readOnly = true)
    @Override
    public List<PaymentResponseDto> getPaymentByEntity(ServiceEntity entity) {
        List<Payment> payments = paymentRepository.findByServiceEntity(entity);

        log.info("Total payments found for entity {}: {}", entity, payments.size());

        return payments.stream()
                .map(paymentMapper::toDto)
                .collect(Collectors.toList());
    }

                  // HELPERS //

    private Account getOwnedAccount(Long accountId, String email) {
        return accountRepository.findByIdAndUsersEmail(accountId, email)
                .orElseThrow(()-> new ResourceNotFoundException("Account not found"));
    }

    private void validateAccountActive(Account account) {
        if (account.getStatusAccount() != StatusAccount.ACTIVE) {
            log.warn("[PAYMENT_REJECTED] accountId={} reason= Account is not active", account.getId());

            auditLogService.registerPaymentEvent(
                    account.getUsers().getId(),
                    "Rejected payment. accountId=" + account.getId() + ", reason=Inactive account",
                    AuditType.TRANSACTION,
                    PaymentAuditAction.PAYMENT_REJECTED_INACTIVE_ACCOUNT
            );
            throw new BusinessException("The account is not active");
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("[PAYMENT_REJECTED] reason= Invalid payment amount amount={}", amount);
            throw new BusinessException("The payment amount must be greater than zero");
        }
        if (amount.scale() > 2) {
            log.warn("[PAYMENT_REJECTED] reason=Invalid decimal scale amount={}", amount);
            throw new BusinessException("The payment amount cannot have more than 2 decimal places");
        }
    }

    private void validateSufficientBalance(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            log.warn("[PAYMENT_REJECTED] accountId={} reason=Insufficient balance amount={} balance={}",
                    account.getId(), amount, account.getBalance());

            auditLogService.registerPaymentEvent(
                    account.getUsers().getId(),
                    "Rejected payment. accountId=" + account.getId() + ", amount=" + amount + ", reason=Insufficient balance",
                    AuditType.TRANSACTION,
                    PaymentAuditAction.PAYMENT_REJECTED_INSUFFICIENT_BALANCE
            );
            throw new BusinessException("Insufficient balance to make the payment");
        }
    }
}
