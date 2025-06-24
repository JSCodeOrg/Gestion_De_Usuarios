package com.JSCode.GestionUsuarios.exception;

import org.springframework.http.HttpStatus;

public class DeactivatedUserException extends AppException{
    public DeactivatedUserException(String message){
        super(message, HttpStatus.FORBIDDEN);
    }
}
