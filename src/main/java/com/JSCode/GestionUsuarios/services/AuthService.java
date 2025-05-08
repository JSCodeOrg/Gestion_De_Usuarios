package com.JSCode.GestionUsuarios.services;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.JSCode.GestionUsuarios.dto.ApiResponse;
import com.JSCode.GestionUsuarios.dto.Auth.AuthResponse;
import com.JSCode.GestionUsuarios.dto.Auth.CheckLogin;
import com.JSCode.GestionUsuarios.dto.Auth.UserCredentials;
import com.JSCode.GestionUsuarios.exceptions.DeactivatedUserException;
import com.JSCode.GestionUsuarios.exceptions.InvalidCredentialsException;
import com.JSCode.GestionUsuarios.exceptions.UserNotVerifiedException;
import com.JSCode.GestionUsuarios.models.Person;
import com.JSCode.GestionUsuarios.models.User;
import com.JSCode.GestionUsuarios.models.UserPerRole;
import com.JSCode.GestionUsuarios.repositories.PersonRepository;
import com.JSCode.GestionUsuarios.repositories.UserPerRoleRepository;
import com.JSCode.GestionUsuarios.repositories.UserRepository;
import com.JSCode.GestionUsuarios.security.JwtUtil;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final JwtUtil jwtUtil;
    private final UserPerRoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository, PersonRepository personRepository, JwtUtil jwtUtil,
            UserPerRoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.personRepository = personRepository;
        this.jwtUtil = jwtUtil;
        this.roleRepository = roleRepository;
    }

    public AuthResponse authenticate(UserCredentials userCredentials) {

        System.out.println(userRepository.existsByMail(userCredentials.getMail()));

        User user = userRepository.findByMail(userCredentials.getMail())
                .orElseThrow(() -> new InvalidCredentialsException("Usuario no encontrado"));

        if (user.getDeleted_at() != null) {
            throw new DeactivatedUserException("Usuario desactivado");
        }

        if (!user.isVerified()) {
            throw new UserNotVerifiedException("Usuario no verificado");
        }

        List<UserPerRole> userPerRoles = roleRepository.findByUser(user);

        List<Long> roleIds = userPerRoles.stream()
                .map(userPerRole -> userPerRole.getRole().getId())
                .collect(Collectors.toList());

        boolean isFirstLogin = user.getFirstLogin();
        if (isFirstLogin) {
            user.setFirstLogin(false);
            userRepository.save(user);
        }

        if (!passwordEncoder.matches(userCredentials.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Nombre de usuario o contraseña incorrectos");
        }

        user.setFirstLogin(false);

        String token = jwtUtil.generateToken(user.getId(), roleIds);

        return new AuthResponse(token, user.getId(), isFirstLogin);
    }

    public ApiResponse<CheckLogin> checkLogin(String token) {

        if (token == null || token.isEmpty()) {
            return new ApiResponse<>("Token no proporcionado", null, true, 401); // Cambiado a 401 para error de
                                                                                 // autorización
        }

        boolean isValid = jwtUtil.isTokenValid(token);

        if (!isValid) {
            return new ApiResponse<>("El token de autenticación no es válido", true, 401); // Cambiado a 401 para error
                                                                                           // de autorización
        }

        Long userId = Long.parseLong(jwtUtil.extractUsername(token));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidCredentialsException("Usuario no encontrado"));

        Person person = personRepository.findByUser(user)
                .orElseThrow(() -> new InvalidCredentialsException("No se encontró información de la persona"));

        List<UserPerRole> userPerRoles = roleRepository.findByUser(user);
        if (userPerRoles.isEmpty()) {
            throw new InvalidCredentialsException("No se encontró información de los roles");
        }
        UserPerRole userPerRole = userPerRoles.get(0);

        if (user.getDeleted_at() != null) {
            throw new DeactivatedUserException("Usuario desactivado");
        }

        if (user.getVerified() == null) {
            throw new InvalidCredentialsException("Usuario no verificado");
        }

        CheckLogin userData = new CheckLogin();
        userData.setUser_id(user.getId());
        userData.setProfileImgUrl(person.getProfileImageUrl());
        userData.setRole(userPerRole.getRole().getName()); // Assuming 'getName()' retrieves the role name as a String

        return new ApiResponse<>("Usuario autenticado con éxito", userData, false, 200);
    }
}
