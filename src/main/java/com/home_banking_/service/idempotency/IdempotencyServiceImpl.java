package com.home_banking_.service.idempotency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.home_banking_.enums.IdempotencyOperation;
import com.home_banking_.enums.IdempotencyStatus;
import com.home_banking_.exceptions.custom.IdempotencyConflictException;
import com.home_banking_.exceptions.custom.ResourceNotFoundException;
import com.home_banking_.model.IdempotencyRecord;
import com.home_banking_.model.Users;
import com.home_banking_.repository.IdempotencyRecordRepository;
import com.home_banking_.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class IdempotencyServiceImpl implements IdempotencyService{

    private static final int EXPIRATION_HOURS = 24;

    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final UsersRepository usersRepository;
    private final RequestHashService requestHashService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public IdempotencyValidationResult validateAndRegister(
            String idempotencyKey,
            Long userId,
            IdempotencyOperation operation,
            Object request
    ) {
        validateKey(idempotencyKey);

        String requestHash = requestHashService.generateHash(request);

        IdempotencyRecord existingRecord;

        if (userId == null) {
            existingRecord = idempotencyRecordRepository
                    .findByIdempotencyKeyAndUserIsNullAndOperation(idempotencyKey, operation)
                    .orElse(null);
        } else {
            existingRecord = idempotencyRecordRepository
                    .findByIdempotencyKeyAndUserIdAndOperation(idempotencyKey, userId, operation)
                    .orElse(null);
        }

        if (existingRecord != null) {
            validateSamePayload(existingRecord, requestHash);

            if (existingRecord.getStatus() == IdempotencyStatus.COMPLETED) {
                return IdempotencyValidationResult.replay(existingRecord);
            }

            if (existingRecord.getStatus() == IdempotencyStatus.PROCESSING) {
                return IdempotencyValidationResult.processing(existingRecord);
            }

            throw new IdempotencyConflictException(
                    "Idempotency key already exists with FAILED status. Please generate a new key."
            );
        }

        Users user = null;

        if (userId != null) {
            user = usersRepository.findById(userId)
                    .orElseThrow(()-> new ResourceNotFoundException("User not found with id:" + userId));
        }

        IdempotencyRecord newRecord = IdempotencyRecord.builder()
                .idempotencyKey(idempotencyKey)
                .user(user)
                .operation(operation)
                .requestHash(requestHash)
                .status(IdempotencyStatus.PROCESSING)
                .expiresAt(LocalDateTime.now().plusHours(EXPIRATION_HOURS))
                .build();

        IdempotencyRecord savedRecord = idempotencyRecordRepository.save(newRecord);

        return IdempotencyValidationResult.newRequest(savedRecord);
    }


    @Override
    @Transactional
    public void markAsCompleted(Long recordId, int responseStatusCode, String responseBody, Long resourceId) {
        IdempotencyRecord record = getRecordById(recordId);

        record.setStatus(IdempotencyStatus.COMPLETED);
        record.setResponseStatusCode(responseStatusCode);
        record.setResponseBody(responseBody);
        record.setResourceId(resourceId);

        idempotencyRecordRepository.save(record);
    }

    @Override
    @Transactional
    public void markAsFailed(Long recordId, String errorMessage) {
        IdempotencyRecord record = getRecordById(recordId);

        record.setStatus(IdempotencyStatus.FAILED);
        record.setErrorMessage(errorMessage);

        idempotencyRecordRepository.save(record);
    }


    @Override
    public <T> T executeAndComplete(
            IdempotencyRecord record,
            Supplier<T> operation,
            Function<T, Long> resourceIdExtractor,
            HttpStatus status,
            String fallbackErrorMessage
    ) {
        try {
            T response = operation.get();

            String responseBody = objectMapper.writeValueAsString(response);

            markAsCompleted(
                    record.getId(),
                    status.value(),
                    responseBody,
                    resourceIdExtractor.apply(response)
            );

            return response;

        } catch (JsonProcessingException ex) {

            markAsFailed(record.getId(), "Error serializing idempotent response");

            throw new RuntimeException("Could not complete idempotent operation");

        } catch (RuntimeException ex) {
            String errorMessage = ex.getMessage() != null
                    ? ex.getMessage()
                    : fallbackErrorMessage;

            markAsFailed(record.getId(), errorMessage);

            throw ex;
        }
    }


    private void validateKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IdempotencyConflictException("Idempotency-Key header is required");
        }

        if (idempotencyKey.length() > 255) {
            throw new IdempotencyConflictException("Idempotency-Key must not exceed 255 characters");
        }
    }

    private void validateSamePayload(IdempotencyRecord record, String requestHash) {
        if (!record.getRequestHash().equals(requestHash)) {
            throw new IdempotencyConflictException(
                    "Idempotency-Key already used with a different request payload"
            );
        }
    }

    private IdempotencyRecord getRecordById(Long recordId) {
        return idempotencyRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Idempotency record not found with id: " + recordId
                ));
    }

    }

