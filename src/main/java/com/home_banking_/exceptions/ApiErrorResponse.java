package com.home_banking_.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ApiErrorResponse {

    private int status;
    private String message;
    private LocalDateTime timestamp;
}
