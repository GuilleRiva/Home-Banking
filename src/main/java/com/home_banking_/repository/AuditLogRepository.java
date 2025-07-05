package com.home_banking_.repository;

import com.home_banking_.enums.AuditType;
import com.home_banking_.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUsers_IdOrderByDateTimeDesc(Long user_id);
    List<AuditLog> findAllByOrderByDateTimeDesc(AuditType auditType);
}
