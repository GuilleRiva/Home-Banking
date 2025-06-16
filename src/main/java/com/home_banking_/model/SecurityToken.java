package com.home_banking_.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "security_token")
public class SecurityToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String token;
    private boolean expired;
    private boolean revoked;
    private LocalDateTime createIn;
    private LocalDateTime expiration;

}
