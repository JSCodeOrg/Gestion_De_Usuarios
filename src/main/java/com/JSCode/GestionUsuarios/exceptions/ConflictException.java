package com.JSCode.GestionUsuarios.exceptions;

import org.springframework.http.HttpStatus;

public class ConflictException extends AppException {
    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}