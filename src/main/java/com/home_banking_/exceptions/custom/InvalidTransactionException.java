package com.home_banking_.exceptions.custom;

import com.home_banking_.exceptions.ApiException;

public class InvalidTransactionException extends ApiException {

    public InvalidTransactionException(String message) {
        super(message);
    }
}
