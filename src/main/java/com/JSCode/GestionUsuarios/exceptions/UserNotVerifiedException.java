package com.JSCode.GestionUsuarios.exceptions;

import org.springframework.http.HttpStatus;

public class UserNotVerifiedException extends AppException {
    public UserNotVerifiedException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
