package com.home_banking_.exceptions;

public abstract class ApiException extends RuntimeException{

    protected ApiException(String message) {
        super(message);
    }
}
