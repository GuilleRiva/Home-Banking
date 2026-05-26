package com.home_banking_.service.idempotency;

import com.home_banking_.enums.IdempotencyOperation;
import com.home_banking_.model.IdempotencyRecord;
import org.springframework.http.HttpStatus;

import java.util.function.Function;
import java.util.function.Supplier;

public interface IdempotencyService {

    IdempotencyValidationResult validateAndRegister(
            String idempotencyKey,
            Long userId,
            IdempotencyOperation operation,
            Object request
    );

    void markAsCompleted(Long recordId, int responseStatusCode, String responseBody, Long resourceId);

    void markAsFailed(Long recordId, String errorMessage);

    <T> T executeAndComplete(
            IdempotencyRecord record,
            Supplier<T> operation,
            Function<T, Long> resourceIdExtractor,
            HttpStatus status,
            String fallbackErrorMessage
    ) ;

}