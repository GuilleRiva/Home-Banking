package com.home_banking_.model;

import com.home_banking_.enums.Rol;
import com.home_banking_.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, length = 100)
    private String surname;
    @Column(nullable = false, unique = true, length = 150)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(name = "dni", nullable = false, unique = true, length = 20)
    private String dni;
    @Column(nullable = false)
    private LocalDateTime registrationDate;

    private int failedLoginAttempts;
    private boolean accountLocked;
    protected LocalDateTime lockTime;

    @Enumerated(EnumType.STRING)
    private Rol rol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserStatus userStatus;

    @OneToMany(mappedBy = "users", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Account> accounts;

    @OneToMany(mappedBy ="users", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AuditLog> auditLogs;

    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Notification> notifications;

    @OneToMany(mappedBy = "users", cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    private List<IPAddress> ipAddresses;

}
