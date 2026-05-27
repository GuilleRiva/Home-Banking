package com.home_banking_.exceptions.custom;

public class IdempotencyConflictException extends RuntimeException{

    public IdempotencyConflictException(String message) {
        super(message);
    }
}
