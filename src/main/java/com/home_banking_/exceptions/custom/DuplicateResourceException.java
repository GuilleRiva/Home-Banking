package com.home_banking_.exceptions.custom;

import com.home_banking_.exceptions.ApiException;

public class DuplicateResourceException extends ApiException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
