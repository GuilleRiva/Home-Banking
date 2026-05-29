package com.home_banking_.enums.audit;

public enum PaymentAuditAction implements AuditAction{
    PAYMENT_COMPLETED,
    PAYMENT_REJECTED_INACTIVE_ACCOUNT,
    PAYMENT_REJECTED_INSUFFICIENT_BALANCE,
}
