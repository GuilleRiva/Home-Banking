package com.home_banking_.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.home_banking_.dto.request.PaymentRequestDto;
import com.home_banking_.dto.request.ServicePaymentRequestDto;
import com.home_banking_.dto.response.PaymentResponseDto;
import com.home_banking_.enums.*;
import com.home_banking_.enums.audit.AuditType;
import com.home_banking_.enums.audit.PaymentAuditAction;
import com.home_banking_.exceptions.custom.*;
import com.home_banking_.mappers.PaymentMapper;
import com.home_banking_.model.Account;
import com.home_banking_.model.Payment;
import com.home_banking_.repository.AccountRepository;
import com.home_banking_.repository.PaymentRepository;
import com.home_banking_.service.AuditLogService;
import com.home_banking_.service.PaymentService;
import com.home_banking_.service.idempotency.IdempotencyService;
import com.home_banking_.service.idempotency.IdempotencyValidationResult;
import com.home_banking_.service.security.CurrentUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    public PaymentServiceImpl(PaymentRepository paymentRepository, AccountRepository accountRepository, PaymentMapper paymentMapper, CurrentUserService currentUserService, AuditLogService auditLogService, IdempotencyService idempotencyService, ObjectMapper objectMapper) {
        this.paymentRepository = paymentRepository;
        this.accountRepository = accountRepository;
        this.paymentMapper = paymentMapper;
        this.currentUserService = currentUserService;
        this.auditLogService = auditLogService;
        this.idempotencyService = idempotencyService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    @Override
    public PaymentResponseDto makePayment(String idempotencyKey, PaymentRequestDto dto)  {
        Long userId = currentUserService.getCurrentUserId();

        log.info("[PAYMENT_IDEMPOTENCY_INIT] userId={} accountId={} amount={}",
                userId, dto.getAccountId(), dto.getAmount());

        IdempotencyValidationResult result = idempotencyService.validateAndRegister(
                idempotencyKey,
                userId,
                IdempotencyOperation.PAYMENT,
                dto
        );

        if (result.isReplay()) {
            log.info("[PAYMENT_IDEMPOTENCY_REPLAY] userId={} recordId={}",
                    userId,result.getRecord().getId());

            return deserializePaymentResponse(result.getRecord().getResponseBody());
        }
        if (result.isProcessing()) {
            log.warn("[PAYMENT_IDEMPOTENCY_PROCESSING] userId={} recordId={}",
                    userId, result.getRecord().getId());

            throw new BusinessException("This payment is currently being processed");
        }

        return idempotencyService.executeAndComplete(
                result.getRecord(),
                ()-> executePayment(dto),
                PaymentResponseDto::getId,
                HttpStatus.CREATED,
                "Unexpected error during payment"
        );
    }


    private PaymentResponseDto executePayment(PaymentRequestDto dto) {
        String email = currentUserService.getCurrentUserEmail();

        log.info("[PAYMENT_INIT] userEmail={} accountId={} amount={}",
                email, dto.getAccountId(), dto.getAmount());

        validatePaymentRequest(dto);

        Account account = getOwnedAccountForUpdate(dto.getAccountId(), email);

        validateAccountCanMakePayment(account,dto.getAmount());

        debitAccount(account,dto.getAmount());

        Payment payment = buildPayment(dto,account);

        Payment savedPayment = paymentRepository.save(payment);
        accountRepository.save(account);

        registerPaymentCompletedAudit(account,savedPayment);

        log.info("[PAYMENT_SUCCESS] userEmail={} accountId={} paymentId={} amount={}",
                email, account.getId(),savedPayment.getId(), savedPayment.getAmount());

        return paymentMapper.toDto(savedPayment);

    }

    @Transactional
    @Override
    public PaymentResponseDto payService(String idempotencyKey, ServicePaymentRequestDto dto) {
        Long userId = currentUserService.getCurrentUserId();

        log.info("[PAYMENT_SERVICE_IDEMPOTENCY_INIT] userId={} accountId={} amount={} serviceEntity={}",
                userId, dto.getAccountId(), dto.getAmount(), dto.getServiceEntity());

        IdempotencyValidationResult result = idempotencyService.validateAndRegister(
                idempotencyKey,
                userId,
                IdempotencyOperation.PAYMENT_SERVICE,
                dto
        );

        if (result.isReplay()) {
            log.info("[PAYMENT_SERVICE_IDEMPOTENCY_REPLAY] userId={} recordId={}",
                    userId,result.getRecord().getId());

            return deserializePaymentResponse(result.getRecord().getResponseBody());
        }
        if (result.isProcessing()) {
            log.warn("[PAYMENT_SERVICE_IDEMPOTENCY_PROCESSING] userId={} recordId={}",
                    userId, result.getRecord().getId());

            throw new BusinessException("This service payment is currently being processed");
        }

        PaymentResponseDto paymentResponseDto = executePayService(dto);

        idempotencyService.markAsCompleted(
                result.getRecord().getId(),
                HttpStatus.CREATED.value(),
                serializePaymentResponse(paymentResponseDto),
                paymentResponseDto.getId()
        );

        log.info("[PAYMENT_SERVICE_IDEMPOTENCY_COMPLETED] userId={} paymentId={} recordId={}",
                userId, paymentResponseDto.getId(), result.getRecord().getId());

        return paymentResponseDto;
    }

    private PaymentResponseDto executePayService(ServicePaymentRequestDto dto) {
        String email = currentUserService.getCurrentUserEmail();

        log.info("[PAYMENT_SERVICE_INIT] userEmail={} accountId={} amount={} serviceEntity={}",
                email,dto.getAccountId(), dto.getAmount(), dto.getServiceEntity());

        validateAmount(dto.getAmount());

        Account account = getOwnedAccountForUpdate(dto.getAccountId(), email);

        validateAccountCanMakePayment(account,dto.getAmount());

        debitAccount(account, dto.getAmount());

        Payment payment = buildServicePayment(dto,account);

        accountRepository.save(account);
        Payment savedPayment = paymentRepository.save(payment);

        log.info("[PAYMENT_SERVICE_SUCCESS] userEmail={} accountId={} paymentId={} amount={}, serviceEntity={}",
                email, account.getId(),savedPayment.getId(), savedPayment.getAmount(), savedPayment.getServiceEntity());

        auditLogService.registerEvent(
                account.getUsers().getId(),
                "Service payment completed. accountId= " + account.getId()
                + ", serviceEntity= " + dto.getServiceEntity()
                + ", amount= " + dto.getAmount(),
                PaymentAuditAction.PAYMENT_COMPLETED,
                AuditType.PAYMENT
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

        log.info("Total payments found for entity={}: payments={}", entity, payments.size());

        return payments.stream()
                .map(paymentMapper::toDto)
                .collect(Collectors.toList());
    }


    private Account getOwnedAccount(Long accountId, String email) {
        return accountRepository.findByIdAndUsersEmail(accountId, email)
                .orElseThrow(()-> new ResourceNotFoundException("Account not found"));
    }

    private Payment buildPayment(PaymentRequestDto dto, Account account) {
        Payment payment = new Payment();

        payment.setDescription(dto.getDescription());
        payment.setAmount(dto.getAmount());
        payment.setAccount(account);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatusPayment(StatusPayment.COMPLETED);

        return payment;
    }

    private void validateAccountActive(Account account) {
        if (account.getStatusAccount() != StatusAccount.ACTIVE) {
            log.warn("[PAYMENT_REJECTED] accountId={} reason= Account is not active", account.getId());

            auditLogService.registerEvent(
                    account.getUsers().getId(),
                    "Rejected payment. accountId= " + account.getId() + ", reason=Inactive account",
                    PaymentAuditAction.PAYMENT_REJECTED_INACTIVE_ACCOUNT,
                    AuditType.PAYMENT
            );
            throw new AccountStateException("The account is not active");
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("[PAYMENT_REJECTED] reason= Invalid payment amount amount={}", amount);
            throw new InsufficientFundsException("The payment amount must be greater than zero");
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

            auditLogService.registerEvent(
                    account.getUsers().getId(),
                    "Rejected payment. accountId= " + account.getId() + ", amount= " + amount + ", reason=Insufficient balance",
                    PaymentAuditAction.PAYMENT_REJECTED_INSUFFICIENT_BALANCE,
                    AuditType.PAYMENT
            );
            throw new InsufficientFundsException("Insufficient balance to make the payment");
        }
    }

    private void validatePaymentRequest(PaymentRequestDto dto) {
        validateAmount(dto.getAmount());

        if (dto.getAccountId() == null) {
            throw new AccountStateException("Account ID is required");
        }
        if (dto.getDescription() == null || dto.getDescription().isBlank()) {
            throw new BusinessException("Payment description is required");
        }
    }

    private void validateAccountCanMakePayment(Account account,BigDecimal amount) {
        validateAccountActive(account);
        validateSufficientBalance(account, amount);
    }

    private PaymentResponseDto deserializePaymentResponse(String responseBody) {
        try {
            return objectMapper.readValue(responseBody, PaymentResponseDto.class);
        } catch (JsonProcessingException e) {
            log.error("[PAYMENT_IDEMPOTENCY_DESERIALIZATION_ERROR]", e);

            throw new IdempotencyConflictException("The payment could not be processed.");
        }
    }

    private void debitAccount(Account account, BigDecimal amount) {
        account.setBalance(account.getBalance().subtract(amount));
    }

    private void registerPaymentCompletedAudit(Account account, Payment payment) {

        auditLogService.registerEvent(
                account.getUsers().getId(),
                "Payment completed. paymentId= " + payment.getId()
                        + ", accountId= " + account.getId()
                        + ", amount= " + payment.getAmount(),
                PaymentAuditAction.PAYMENT_COMPLETED,
                AuditType.PAYMENT
        );
    }

    private String serializePaymentResponse(PaymentResponseDto response) {
        try {
            return objectMapper.writeValueAsString(response);
        }catch (JsonProcessingException e) {
            log.error("[PAYMENT_IDEMPOTENCY_SERIALIZATION_ERROR] loanId={}",
                    response.getId(), e);

            throw new IdempotencyConflictException("Could not serialize payment response");
        }
    }

    private Account getOwnedAccountForUpdate(Long accountId, String email) {
        return accountRepository.findByIdAndUsersEmailForUpdate(accountId, email)
                .orElseThrow(()-> new ResourceNotFoundException("Account not found"));
    }

    private Payment buildServicePayment(ServicePaymentRequestDto dto, Account account) {
        Payment payment = new Payment();

        payment.setAccount(account);
        payment.setAmount(dto.getAmount());
        payment.setDescription(dto.getDescription());
        payment.setServiceEntity(dto.getServiceEntity());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatusPayment(StatusPayment.COMPLETED);

        return payment;
    }
}
