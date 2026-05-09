package com.home_banking_.service.idempotency;

import com.home_banking_.enums.IdempotencyOperation;

public interface IdempotencyService {

    IdempotencyValidationResult validateAndRegister(
            String idempotencyKey,
            Long userId,
            IdempotencyOperation operation,
            Object request
    );

    void markAsCompleted(Long recordId, int responseStatusCode, String responseBody, Long resourceId);

    void markAsFailed(Long recordId, String errorMessage);

}