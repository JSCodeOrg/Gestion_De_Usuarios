package com.JSCode.GestionUsuarios.exception;

import org.springframework.http.HttpStatus;

public class UserNotVerifiedException extends AppException {
    public UserNotVerifiedException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
