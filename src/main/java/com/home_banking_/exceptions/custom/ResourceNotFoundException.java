package com.home_banking_.exceptions.custom;

import com.home_banking_.exceptions.ApiException;

public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
