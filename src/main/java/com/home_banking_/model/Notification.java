package com.home_banking_.model;

import com.home_banking_.enums.TypeNotification;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String message;
    private LocalDateTime shippingDate;
    private Boolean read;
    private TypeNotification typeNotification;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users users;
}
