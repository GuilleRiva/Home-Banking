package com.home_banking_.service;

import com.home_banking_.dto.response.AuditLogResponseDto;
import com.home_banking_.enums.*;

import java.util.List;

public interface AuditLogService {

    void registerPaymentEvent(Long userId, String description,PaymentAuditAction paymentAction, AuditType auditType);

    void registerEvent(Long userId, String message, String typeEvent, String type);

    List<AuditLogResponseDto> getLogsByUser(Long userId);

    List<AuditLogResponseDto>getLogsByType(String type);

}
