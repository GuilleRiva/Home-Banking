package com.home_banking_.model;

import java.time.LocalDateTime;

public class SecurityToken {
    private Long id;
    private String token;
    private boolean expired;
    private boolean revoked;
    private LocalDateTime createIn;
    private LocalDateTime expiration;

}
