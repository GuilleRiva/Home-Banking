package com.home_banking_.model;

import com.home_banking_.enums.StatusCard;
import com.home_banking_.enums.TypeCard;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String number;
    private LocalDateTime expiration;
    private String CVV;

    @Enumerated(EnumType.STRING)
    private TypeCard typeCard;

    @Enumerated(EnumType.STRING)
    private StatusCard statusCard;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users users;
}
