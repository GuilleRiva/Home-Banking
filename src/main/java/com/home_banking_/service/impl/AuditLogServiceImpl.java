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
import com.home_banking_.service.security.CurrentUserService;
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
    private final CurrentUserService currentUserService;


    public AuditLogServiceImpl(AuditLogRepository auditLogRepository, UsersRepository usersRepository, AuditLogMapper auditLogMapper, CurrentUserService currentUserService) {
        this.auditLogRepository = auditLogRepository;
        this.usersRepository = usersRepository;
        this.auditLogMapper = auditLogMapper;

        this.currentUserService = currentUserService;
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
        String requesterEmail = currentUserService.getCurrentUserEmail();

        log.info("[ADMIN_AUDIT_LOGS_BY_USER_INIT] requesterEmail={} requestedUserId={}",
                requesterEmail, userId);

        validateUserId(userId);
        log.info("[AUDIT_LOGS_BY_USER_DEBUG_2] userId validated");

        if (!usersRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }


        List<AuditLog> logs = auditLogRepository.findByUsers_IdOrderByDateTimeDesc(userId);

        log.info("[ADMIN_AUDIT_LOGS_BY_USER_SUCCESS] requesterEmail={} requestedUserId={} totalLogs={}",
                requesterEmail, userId, logs.size());

        List<AuditLogResponseDto> response = logs.stream()
                .map(auditLogMapper::toDTO)
                .toList();

        log.info("[AUDIT_LOGS_BY_USER_DEBUG_5] logs mapped");

        return response;
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


    private void validateUserId(Long userId){
        if (userId == null) {
            throw new BusinessException("User ID is required");
        }
    }



}
