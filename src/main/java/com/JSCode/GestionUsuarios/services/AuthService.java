package com.JSCode.GestionUsuarios.services;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.JSCode.GestionUsuarios.dto.UserCredentials;
import com.JSCode.GestionUsuarios.exceptions.InvalidCredentialsException;
import com.JSCode.GestionUsuarios.models.User;
import com.JSCode.GestionUsuarios.repositories.UserRepository;
import com.JSCode.GestionUsuarios.security.JwtUtil;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository,  JwtUtil jwtUtil){
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public String authenticate(UserCredentials userCredentials){
        User user = userRepository.findByMail(userCredentials.getMail())
        .orElseThrow(() -> new InvalidCredentialsException("Usuario no encontrado"));

        if(!passwordEncoder.matches(userCredentials.getPassword(), user.getPassword())){
            throw new InvalidCredentialsException("Nombre de usuario o contraseña incorrectos");
        }

        return jwtUtil.generateToken(user.getMail());
    }
}

