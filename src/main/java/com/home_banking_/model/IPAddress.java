package com.home_banking_.model;

import jakarta.persistence.*;
import lombok.Data;


import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ip_address")
public class IPAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String directionIP;
    private LocalDateTime registrationDate;
    private boolean suspicious;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users users;

}
