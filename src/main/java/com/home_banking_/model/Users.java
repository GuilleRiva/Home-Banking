package com.home_banking_.model;

import com.home_banking_.enums.Rol;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String surname;
    private String email;
    private String password;
    private String DNI;
    private LocalDateTime registrationDate;

    @Enumerated(EnumType.STRING)
    private Rol rol;

    @OneToMany(mappedBy = "users", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Account> accounts;

    @OneToMany(mappedBy ="users", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AuditLog> auditLogs;

    @OneToMany(mappedBy = "users", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Notification> notifications;

    @OneToMany(mappedBy = "users", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Card> cards;

    @OneToMany(mappedBy = "users", cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    private List<IPAddress> ipAddresses;

}
