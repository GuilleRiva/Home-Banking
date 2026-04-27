package com.home_banking_.service.idempotency;


public interface RequestHashService {
    String generateHash(Object request);
}
