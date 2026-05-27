package com.home_banking_.exceptions.custom;

import com.home_banking_.exceptions.ApiException;

public class InsufficientFundsException extends ApiException {

    public InsufficientFundsException(String message) {
        super(message);
    }
}
