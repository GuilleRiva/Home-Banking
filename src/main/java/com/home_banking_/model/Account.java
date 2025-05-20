package com.home_banking_.model;

import com.home_banking_.enums.StatusAccount;
import com.home_banking_.enums.TypeAccount;

import java.time.LocalDateTime;

public class Account {
    private Long id;
    private String accountNumber;
    private String alias;
    private String CBU;
    private LocalDateTime creationDate;
    private TypeAccount typeAccount;
    private StatusAccount statusAccount;

}
