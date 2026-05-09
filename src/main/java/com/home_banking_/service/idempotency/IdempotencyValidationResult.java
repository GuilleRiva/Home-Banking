package com.home_banking_.service.idempotency;

import com.home_banking_.model.IdempotencyRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class IdempotencyValidationResult {

    private boolean replay;
    private boolean processing;
    private IdempotencyRecord record;

    public static IdempotencyValidationResult newRequest(IdempotencyRecord record) {
        return IdempotencyValidationResult.builder()
                .replay(false)
                .processing(false)
                .record(record)
                .build();
    }


    public static IdempotencyValidationResult replay(IdempotencyRecord record) {
        return IdempotencyValidationResult.builder()
                .replay(true)
                .processing(false)
                .record(record)
                .build();
    }

    public static IdempotencyValidationResult processing(IdempotencyRecord record) {
        return IdempotencyValidationResult.builder()
                .replay(false)
                .processing(true)
                .record(record)
                .build();
    }

}
