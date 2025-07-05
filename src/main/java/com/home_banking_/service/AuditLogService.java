package com.home_banking_.service;

import com.home_banking_.dto.ResponseDto.AuditLogResponseDto;
import com.home_banking_.model.AuditLog;

import java.util.List;

public interface AuditLogService {

    void registerEvent(Long userId, String message, String typeEvent, String type);

    List<AuditLogResponseDto> getLogsByUser(Long userId);

    List<AuditLogResponseDto>getLogsByType(String type);
}
