package com.home_banking_.repository;

import com.home_banking_.enums.IdempotencyOperation;
import com.home_banking_.model.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, Long> {

    Optional<IdempotencyRecord> findByIdempotencyKeyAndUserIdAndOperation(
            String idempotencyKey,
            Long userId,
            IdempotencyOperation operation
    );

    Optional<IdempotencyRecord> findByIdempotencyKeyAndUserIsNullAndOperation(
            String idempotencyKey,
            IdempotencyOperation operation
    );

    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
