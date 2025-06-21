package com.home_banking_.model;

import com.home_banking_.enums.AuditType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "audit_log")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String action;
    private String description;
    private LocalDateTime dateTime;
    private String ipOrigin;

    @Enumerated(EnumType.STRING)
    private AuditType type;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users users;

}
