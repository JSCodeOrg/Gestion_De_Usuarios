package com.JSCode.GestionUsuarios.controllers;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.JSCode.GestionUsuarios.dto.ApiResponse;
import com.JSCode.GestionUsuarios.dto.DeactivationRequest;
import com.JSCode.GestionUsuarios.dto.RecoverPassword;
import com.JSCode.GestionUsuarios.dto.UserRegisterDto;
import com.JSCode.GestionUsuarios.models.User;
import com.JSCode.GestionUsuarios.services.UserService;
import com.JSCode.GestionUsuarios.services.Email.RecoverEmail;
import com.JSCode.GestionUsuarios.dto.VerificationRequest;


@RestController
@RequestMapping("/users")

public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RecoverEmail recoverEmail;
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> register(@RequestBody UserRegisterDto data) {
        User user = userService.registerUser(data);
        return ResponseEntity.ok(
            new ApiResponse<>("Registro exitoso", user, false, 200)
        );
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Void>> verifyUser(@RequestBody VerificationRequest request) {
        boolean isVerified = userService.verifyUser(request.getMail(), request.getCode());
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

   @PostMapping("/recoverpassword")
    public ResponseEntity<ApiResponse<Void>> emailExists(@RequestBody RecoverPassword request) {
        boolean existUser = userService.emailExists(request.getMail());
        if (existUser) {
            try {
                recoverEmail.sendRecoverEmail(request.getMail());
                return ResponseEntity.ok(
                    new ApiResponse<>("Email verificado correctamente. Se han enviado las instrucciones a tu correo.", null, false, 200)
                );
            } catch (MessagingException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<>("Error al enviar el correo de recuperación", null, true, 500)
                );
            }
        } else {
            return ResponseEntity.badRequest().body(
                new ApiResponse<>("Usuario no registrado", null, true, 400)
            );
        }
    }
}



