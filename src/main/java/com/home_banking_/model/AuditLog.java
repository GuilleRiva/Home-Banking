package com.home_banking_.model;

import java.time.LocalDateTime;

public class AuditLog {
    private Long id;
    private String action;
    private String description;
    private LocalDateTime dateTime;
    private String ipOrigin;

}
