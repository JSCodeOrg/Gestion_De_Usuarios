package com.JSCode.GestionUsuarios.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.JSCode.GestionUsuarios.dto.UserCredentials;
import com.JSCode.GestionUsuarios.dto.UserRegisterDto;
import com.JSCode.GestionUsuarios.models.User;
import com.JSCode.GestionUsuarios.services.UserService;


@RestController
@RequestMapping("/users")

public class UserController {

    @Autowired
    private UserService userService;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegisterDto data){
        try{
            User user = userService.registerUser(data);
            return ResponseEntity.ok(user);
        }
        catch(RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
