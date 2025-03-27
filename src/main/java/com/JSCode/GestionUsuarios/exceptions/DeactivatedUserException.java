package com.JSCode.GestionUsuarios.exceptions;

import org.springframework.http.HttpStatus;

public class DeactivatedUserException extends AppException{
    public DeactivatedUserException(String message){
        super(message, HttpStatus.FORBIDDEN);
    }
}
