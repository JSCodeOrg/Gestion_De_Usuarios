package com.JSCode.GestionUsuarios.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.JSCode.GestionUsuarios.dto.ApiResponse;
import com.JSCode.GestionUsuarios.dto.DeactivationRequest;
import com.JSCode.GestionUsuarios.dto.UserRegisterDto;
import com.JSCode.GestionUsuarios.models.User;
import com.JSCode.GestionUsuarios.services.UserService;
import com.JSCode.GestionUsuarios.dto.VerificationRequest;


@RestController
@RequestMapping("/users")

public class UserController {

    @Autowired
    private UserService userService;
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> register(@RequestBody UserRegisterDto data) {
        User user = userService.registerUser(data);
        return ResponseEntity.ok(
            new ApiResponse<>("Registro exitoso", user, false, 200)
        );
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Void>> verifyUser(@RequestBody VerificationRequest request) {
        boolean isVerified = userService.verifyUser(request.getEmail(), request.getCode());
        if (isVerified) {
            return ResponseEntity.ok(
                new ApiResponse<>("Email verificado correctamente", null, false, 200)
            );
        } else {
            return ResponseEntity.badRequest().body(
                new ApiResponse<>("Codigo de verificacion invalido", null, true, 400)
            );
        }
    }
 
    @PostMapping("/deactivate")
    public ResponseEntity<ApiResponse<String>> requestAccountDeactivation(@RequestBody DeactivationRequest request) {
        userService.DeactivationRequest(request.getMail());
        return ResponseEntity.ok(new ApiResponse<>("Usuario desactivado exitosamente", null, false, 200));
    }
}



