package com.home_banking_.model;

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

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users users;

}
