package com.home_banking_.enums.audit;

public enum LoanAuditAction implements AuditAction{
    LOAN_SIMULATION_COMPLETED,
    LOAN_GRANTED,
    LOAN_REQUEST_CREATED,
    LOAN_REQUEST_REJECTED,
    LOAN_REJECTED,
}
