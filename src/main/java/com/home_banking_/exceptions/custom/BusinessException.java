package com.home_banking_.exceptions.custom;

import com.home_banking_.exceptions.ApiException;

public class BusinessException extends ApiException {

    public BusinessException(String message) {
        super(message);
    }
}
