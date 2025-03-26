package com.JSCode.GestionUsuarios.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.JSCode.GestionUsuarios.dto.UserCredentials;
import com.JSCode.GestionUsuarios.services.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserCredentials userCredentials) {
            String token = authService.authenticate(userCredentials);
            return ResponseEntity.ok(Map.of("token", token));
    }

}
    