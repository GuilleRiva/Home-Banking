package com.home_banking_.exceptions;

public class BusinessException extends RuntimeException{

    public BusinessException(String mensaje){
        super(mensaje);
    }
}
