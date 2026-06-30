package com.home_banking_.service.impl;

import com.home_banking_.dto.response.AuditLogResponseDto;
import com.home_banking_.enums.audit.AuditAction;
import com.home_banking_.enums.audit.AuditType;
import com.home_banking_.exceptions.custom.BusinessException;
import com.home_banking_.exceptions.custom.ResourceNotFoundException;
import com.home_banking_.mappers.AuditLogMapper;
import com.home_banking_.model.AuditLog;
import com.home_banking_.model.Users;
import com.home_banking_.repository.AuditLogRepository;
import com.home_banking_.repository.UsersRepository;
import com.home_banking_.service.AuditLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UsersRepository usersRepository;
    private final AuditLogMapper auditLogMapper;


    public AuditLogServiceImpl(AuditLogRepository auditLogRepository, UsersRepository usersRepository, AuditLogMapper auditLogMapper) {
        this.auditLogRepository = auditLogRepository;
        this.usersRepository = usersRepository;
        this.auditLogMapper = auditLogMapper;

    }

    @Override
    public void registerEvent(Long userId, String message, AuditAction typeEvent, AuditType type) {
        log.info("[LOGGING_REGISTER_EVENT] audit event for userId={} ,TypeEvent={}, Type={}",
                userId, typeEvent, type);

        Users users = usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found"
                ));

        AuditLog logEntity = new AuditLog();
        logEntity.setUsers(users);
        logEntity.setAction(typeEvent.name());
        logEntity.setDescription(message);
        logEntity.setDateTime(LocalDateTime.now());
        logEntity.setIpOrigin("127.0.0.1");
        logEntity.setType(type);

        auditLogRepository.save(logEntity);
        log.info("[AUDIT_REGISTERED_EVENT] event successfully logged for userId={}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponseDto> getLogsByUser(Long userId) {

        if (userId == null) {
            throw new BusinessException("User ID is required");
        }

        if (!usersRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }

        List<AuditLog> logs = auditLogRepository.findByUsers_IdOrderByDateTimeDesc(userId);

        log.info("[AUDIT_LOGS_FETCH_BY_USER] userId={} totalLogs={}", userId, logs.size());

        return logs.stream()
                .map(auditLogMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponseDto> getLogsByType(String type) {

        AuditType auditType;
        try {
            auditType = AuditType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("[INVALID_LOG_EVENT] Invalid audit type when Fetching logs: {}", type);
            throw new BusinessException("Invalid audit type: " + type);
        }

        List<AuditLog> logs = auditLogRepository.findByTypeOrderByDateTimeDesc(auditType);

        log.debug("Total logs found for type {}: {}", auditType, logs.size());

        return logs.stream()
                .map(auditLogMapper::toDTO)
                .toList();
    }

}
