package com.JSCode.GestionUsuarios.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import com.JSCode.GestionUsuarios.dto.Response;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<Response<?>> handleAppException(AppException ex) {
        return ResponseEntity
            .status(ex.getStatus())
            .body(new Response<>(
                ex.getMessage(), 
                null, 
                true, 
                ex.getStatus().value()
            ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<?>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .findFirst()
            .orElse("Error de validación");
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new Response<>(errorMessage, true, HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<?>> handleGenericException(Exception ex) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new Response<>("Error interno del servidor", true, 
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}