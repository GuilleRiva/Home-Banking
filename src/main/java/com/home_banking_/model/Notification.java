package com.home_banking_.model;

import com.home_banking_.enums.TypeNotification;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(columnDefinition = "TEXT")
    private String message;
    @Column(name = "shipping_date")
    private LocalDateTime shippingDate;
    @Column(name = "is_read", nullable = false)
    private Boolean read;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_notification", nullable = false)
    private TypeNotification typeNotification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users users;
}
