package com.JSCode.GestionUsuarios.exceptions;

import org.springframework.http.HttpStatus;

public class NotFoundException extends AppException {
    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
