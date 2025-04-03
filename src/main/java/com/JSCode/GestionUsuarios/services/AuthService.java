package com.JSCode.GestionUsuarios.services;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.JSCode.GestionUsuarios.dto.Auth.AuthResponse;
import com.JSCode.GestionUsuarios.dto.Auth.UserCredentials;
import com.JSCode.GestionUsuarios.exceptions.DeactivatedUserException;
import com.JSCode.GestionUsuarios.exceptions.InvalidCredentialsException;
import com.JSCode.GestionUsuarios.models.Person;
import com.JSCode.GestionUsuarios.models.User;
import com.JSCode.GestionUsuarios.repositories.PersonRepository;
import com.JSCode.GestionUsuarios.repositories.UserRepository;
import com.JSCode.GestionUsuarios.security.JwtUtil;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository, PersonRepository personRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.personRepository = personRepository;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse authenticate(UserCredentials userCredentials) {
        User user = userRepository.findByMail(userCredentials.getMail())
                .orElseThrow(() -> new InvalidCredentialsException("Usuario no encontrado"));
        
        if(user.getDeleted_at() != null){
            throw new DeactivatedUserException("Usuario desactivado");
        }

        if(user.getVerified() == null){
            throw new InvalidCredentialsException("Usuario no verificado");
        }

        Person person = personRepository.findByUser(user)
                .orElseThrow(() -> new InvalidCredentialsException("No se encontró información de la persona"));

        if (!passwordEncoder.matches(userCredentials.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Nombre de usuario o contraseña incorrectos");
        }

        String token = jwtUtil.generateToken(user.getMail());

        return new AuthResponse(token, person.getNombre(), user.getMail(), user.getId(), user.getFirstLogin());
    }
}
