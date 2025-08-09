package com.home_banking_.service.impl;

import com.home_banking_.dto.ResponseDto.AuditLogResponseDto;
import com.home_banking_.enums.AuditType;
import com.home_banking_.exceptions.BusinessException;
import com.home_banking_.exceptions.ResourceNotFoundException;
import com.home_banking_.mappers.AuditLogMapper;
import com.home_banking_.model.AuditLog;
import com.home_banking_.model.Users;
import com.home_banking_.repository.AuditLogRepository;
import com.home_banking_.repository.UsersRepository;
import com.home_banking_.service.AuditLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    public void registerEvent(Long userId, String message, String typeEvent, String type) {
        log.info("Logging audit event for user ID: {} | Action: {} | Type: {}", userId,typeEvent, type);


        Users users = usersRepository.findById(userId)
                .orElseThrow(()-> {
                    log.warn("User not found when registering event. ID: {}", userId);
                    return new ResourceNotFoundException("User not found");
                });


        AuditType auditType;
        try {
            auditType = AuditType.valueOf(type.toUpperCase());
        }catch (IllegalArgumentException e){
            log.error("Invalid audit type received: {}", type);
            throw new BusinessException("Invalid audit type : " + type);
        }


        AuditLog logEntity = new AuditLog();
        logEntity.setUsers(users);
        logEntity.setAction(typeEvent);
        logEntity.setDescription(message);
        logEntity.setDateTime(LocalDateTime.now());
        logEntity.setIpOrigin("127.0.0.1");//Puedo luego obtener esto desde el request HTTP
       logEntity.setType(auditType);


        auditLogRepository.save(logEntity);
        log.info("Audit event successfully logged for user ID: {}", userId);
    }




    @Override
    public List<AuditLogResponseDto> getLogsByUser(Long user_id) {
        log.info("Getting audit logs for user ID: {}", user_id);

        List<AuditLog> logs = auditLogRepository.findByUsers_IdOrderByDateTimeDesc(user_id);
        log.debug("Total logs found for user ID {}: {}", user_id, logs.size());

        return logs.stream()
                .map(auditLogMapper::toDTO)
                .toList();
    }



    @Override
    public List<AuditLogResponseDto> getLogsByType(String type) {
        log.info("Getting audit logs for type {}: ", type);

        AuditType auditType;
        try {
            auditType = AuditType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Invalid audit type when querying logs: {}", type);
            throw new BusinessException("Invalid audit type: " + type);
        }

        List<AuditLog> logs = auditLogRepository.findByTypeOrderByDateTimeDesc(auditType);

        log.debug("Total logs found for type {}: {}", auditType, logs.size());

        return logs.stream()
                .map(auditLogMapper::toDTO)
                .toList();

    }
}
