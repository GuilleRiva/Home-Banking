package com.home_banking_.model;

import com.home_banking_.enums.TypeNotification;

import java.time.LocalDateTime;

public class Notification {
    private Long id;
    private String message;
    private LocalDateTime shippingDate;
    private Boolean read;
    private TypeNotification typeNotification;
}
