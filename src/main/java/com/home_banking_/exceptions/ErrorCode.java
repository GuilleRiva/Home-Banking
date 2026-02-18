package com.home_banking_.exceptions;

public enum ErrorCode {
    RESOURCE_NOT_FOUND("HB-404-001"),
    BUSINESS_RULE_VIOLATION("HB-409-001"),
    VALIDATION_FAILED("HB-400-VAL"),
    INTERNAL_ERROR("HB-500-001");

    private  final String code;

    ErrorCode(String code) { this.code = code;}
    public String code(){return  code;}
}
