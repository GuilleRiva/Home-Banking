package com.home_banking_.exceptions;

public class ResourceNotFoundException extends  RuntimeException{
    public ResourceNotFoundException(String mensaje){
        super(mensaje);
    }
}
