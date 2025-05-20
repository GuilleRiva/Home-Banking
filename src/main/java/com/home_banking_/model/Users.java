package com.home_banking_.model;

import com.home_banking_.enums.Rol;

import java.time.LocalDateTime;

public class Users {
    private Long id;
    private String name;
    private String surname;
    private String email;
    private String password;
    private String DNI;
    private LocalDateTime registrationDate;
    private Rol rol;

}
