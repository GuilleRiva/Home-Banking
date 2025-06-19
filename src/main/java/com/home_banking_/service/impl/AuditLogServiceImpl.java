package com.home_banking_.service.impl;

import com.home_banking_.exceptions.ResourceNotFoundException;
import com.home_banking_.model.AuditLog;
import com.home_banking_.model.Users;
import com.home_banking_.repository.AuditLogRepository;
import com.home_banking_.repository.UsersRepository;
import com.home_banking_.service.AuditLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UsersRepository usersRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository, UsersRepository usersRepository) {
        this.auditLogRepository = auditLogRepository;
        this.usersRepository = usersRepository;
    }


    @Override
    public void registerEvent(Long userId, String message, String typeEvent, String type) {
        Users users = usersRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));

        AuditLog log = new AuditLog();
        log.setUsers(users);
        log.setDateTime(LocalDateTime.now());

        auditLogRepository.save(log);
    }


    @Override
    public List<AuditLog> getLogsByUser(Long userId) {
        return auditLogRepository.findByUser_IdOrderByDateDesc(userId);
    }


    @Override
    public List<AuditLog> getLogsByType(String type) {
        return auditLogRepository.findByTypeOrderByDateDesc(type);
    }
}
