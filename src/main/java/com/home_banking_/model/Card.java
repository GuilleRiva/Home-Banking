package com.home_banking_.model;

import com.home_banking_.enums.TypeCard;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String number;
    private LocalDateTime expiration;
    private String CVV;
    private TypeCard typeCard;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users users;
}
