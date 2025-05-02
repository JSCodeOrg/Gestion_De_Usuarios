package com.JSCode.GestionUsuarios.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.JSCode.GestionUsuarios.dto.ApiResponse;
import com.JSCode.GestionUsuarios.dto.Auth.AuthResponse;
import com.JSCode.GestionUsuarios.dto.Auth.CheckLogin;
import com.JSCode.GestionUsuarios.dto.Auth.UserCredentials;
import com.JSCode.GestionUsuarios.services.AuthService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserCredentials userCredentials, HttpServletResponse response) {
        AuthResponse authResponse = authService.authenticate(userCredentials);
        return ResponseEntity.ok(authResponse);

    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CheckLogin>> checkLogin(
            @RequestHeader("Authorization") String authorizationHeader) {

        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>("Token no proporcionado", null, true, 401));
        }

        if (!authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>("Token en formato incorrecto", null, true, 401));
        }

        String token = authorizationHeader.substring(7); 

        ApiResponse<CheckLogin> verifySession = authService.checkLogin(token);
        if (verifySession.isError()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ApiResponse<>("Token inválido o expirado", null, true, 401));
        }

        return ResponseEntity.ok(verifySession);
    }
}
