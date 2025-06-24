package com.example.GestionUsuarios.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.JSCode.GestionUsuarios.dto.auth.AuthResponse;
import com.JSCode.GestionUsuarios.model.Roles;
import com.JSCode.GestionUsuarios.model.User;
import com.JSCode.GestionUsuarios.model.UserPerRole;
import com.JSCode.GestionUsuarios.repository.UserPerRoleRepository;
import com.JSCode.GestionUsuarios.repository.UserRepository;
import com.JSCode.GestionUsuarios.security.JwtUtil;
import com.JSCode.GestionUsuarios.service.AuthService;

public class AuthControllerTest{
    
}