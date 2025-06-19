package com.home_banking_.service;

import com.home_banking_.model.AuditLog;

import java.util.List;

public interface AuditLogService {

    void registerEvent(Long userId, String message, String typeEvent, String type);
    List<AuditLog> getLogsByUser(Long userId);
    List<AuditLog>getLogsByType(String type);
}
