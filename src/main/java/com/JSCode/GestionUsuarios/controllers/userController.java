package com.JSCode.GestionUsuarios.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.JSCode.GestionUsuarios.models.UserCredentials;
import com.JSCode.GestionUsuarios.services.EmailService;

@RestController
@RequestMapping("/users")
public class userController { // Cambio a PascalCase

    @Autowired
    private EmailService emailService;

    @PostMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestBody UserCredentials credentials) {
        // Validación de entrada
        if (credentials == null || credentials.getMail() == null || credentials.getMail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El email es requerido");
        }
        
        String email = credentials.getMail().trim().toLowerCase(); // Normalización
        
        if (!emailService.isValidEmail(email)) {
            return ResponseEntity.badRequest().body("Formato de email inválido");
        }
        
        if (emailService.emailExists(email)) {
            return ResponseEntity.ok().body("El email ya está registrado");
        }
        
        return ResponseEntity.ok().body("Email disponible");
    }
}