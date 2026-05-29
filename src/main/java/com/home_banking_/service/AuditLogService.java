package com.home_banking_.service;

import com.home_banking_.dto.response.AuditLogResponseDto;
import com.home_banking_.enums.audit.AuditAction;
import com.home_banking_.enums.audit.AuditType;
import com.home_banking_.enums.audit.LoanAuditAction;

import java.util.List;

public interface AuditLogService {


    void registerEvent(Long userId, String message, AuditAction typeEvent, AuditType type);

    List<AuditLogResponseDto> getLogsByUser(Long userId);

    List<AuditLogResponseDto>getLogsByType(String type);

}
