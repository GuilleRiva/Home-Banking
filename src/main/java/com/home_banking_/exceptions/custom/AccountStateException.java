package com.home_banking_.exceptions.custom;

import com.home_banking_.exceptions.ApiException;

public class AccountStateException extends ApiException {

    public AccountStateException(String message) {
        super(message);
    }
}
