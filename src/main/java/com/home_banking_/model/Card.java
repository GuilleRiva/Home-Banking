package com.home_banking_.model;

import com.home_banking_.enums.TypeCard;

import java.time.LocalDateTime;

public class Card {
    private Long id;
    private String number;
    private LocalDateTime expiration;
    private String CVV;
    private TypeCard typeCard;

    private Account associated_Account;
}
