package com.example.GestionUsuarios.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.JSCode.GestionUsuarios.dto.api.ApiResponse;
import com.JSCode.GestionUsuarios.dto.auth.AuthResponse;
import com.JSCode.GestionUsuarios.dto.auth.CheckLogin;
import com.JSCode.GestionUsuarios.dto.auth.UserCredentials;
import com.JSCode.GestionUsuarios.exception.DeactivatedUserException;
import com.JSCode.GestionUsuarios.exception.InvalidCredentialsException;
import com.JSCode.GestionUsuarios.exception.UserNotVerifiedException;
import com.JSCode.GestionUsuarios.model.Person;
import com.JSCode.GestionUsuarios.model.Roles;
import com.JSCode.GestionUsuarios.model.User;
import com.JSCode.GestionUsuarios.model.UserPerRole;
import com.JSCode.GestionUsuarios.repository.PersonRepository;
import com.JSCode.GestionUsuarios.repository.UserPerRoleRepository;
import com.JSCode.GestionUsuarios.repository.UserRepository;
import com.JSCode.GestionUsuarios.security.JwtUtil;
import com.JSCode.GestionUsuarios.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
public class AuthServiceTest {

    private UserRepository userRepository;
    private PersonRepository personRepository;
    private UserPerRoleRepository roleRepository;
    private JwtUtil jwtUtil;
    private AuthService authService;
    private BCryptPasswordEncoder encoder;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        personRepository = mock(PersonRepository.class);
        roleRepository = mock(UserPerRoleRepository.class);
        jwtUtil = mock(JwtUtil.class);
        authService = new AuthService(userRepository, personRepository, jwtUtil, roleRepository);
        encoder = new BCryptPasswordEncoder();
    }
    @Test
    void testAuthenticate_success() {
        String rawPassword = "password";
        String encodedPassword = encoder.encode(rawPassword);

        User user = mockUser(1L, encodedPassword, true, null, true);

        // Simulación directa sin método auxiliar
        Roles role = new Roles();
        role.setId(1L);
        role.setName("ADMIN");

        UserPerRole upr = mock(UserPerRole.class);
        when(upr.getRole()).thenReturn(role);

        when(userRepository.findByMail("user@example.com")).thenReturn(Optional.of(user));
        when(roleRepository.findByUser(any(User.class))).thenReturn(List.of(upr));
        when(jwtUtil.generateToken(eq(1L), ArgumentMatchers.anyList())).thenReturn("mock-token");
        when(userRepository.save(any())).thenReturn(user);

        UserCredentials credentials = new UserCredentials();
        credentials.setMail("user@example.com");
        credentials.setPassword(rawPassword);

        AuthResponse response = authService.authenticate(credentials);

        assertNotNull(response);
        assertEquals("mock-token", response.getToken());
        assertEquals(1L, response.getUser_id());
        assertTrue(response.getFirstLogin());
    }

    @Test
    void testAuthenticate_userNotFound() {
        when(userRepository.findByMail("user@example.com")).thenReturn(Optional.empty());

        UserCredentials credentials = new UserCredentials();
        credentials.setMail("user@example.com");
        credentials.setPassword("password");

        assertThrows(InvalidCredentialsException.class, () -> authService.authenticate(credentials));
    }

    @Test
    void testAuthenticate_userNotVerified() {
        String encodedPassword = encoder.encode("password");
        User user = mockUser(1L, encodedPassword, false, null, true);

        when(userRepository.findByMail("user@example.com")).thenReturn(Optional.of(user));

        UserCredentials credentials = new UserCredentials();
        credentials.setMail("user@example.com");
        credentials.setPassword("password");

        assertThrows(UserNotVerifiedException.class, () -> authService.authenticate(credentials));
    }

    @Test
    void testAuthenticate_deactivatedUser() {
        String encodedPassword = encoder.encode("password");
        User user = mockUser(1L, encodedPassword, true, LocalDateTime.now(), true);

        when(userRepository.findByMail("user@example.com")).thenReturn(Optional.of(user));

        UserCredentials credentials = new UserCredentials();
        credentials.setMail("user@example.com");
        credentials.setPassword("password");

        assertThrows(DeactivatedUserException.class, () -> authService.authenticate(credentials));
    }

    @Test
    void testAuthenticate_wrongPassword() {
        String encodedPassword = encoder.encode("correct-password");
        User user = mockUser(1L, encodedPassword, true, null, true);

        when(userRepository.findByMail("user@example.com")).thenReturn(Optional.of(user));

        UserCredentials credentials = new UserCredentials();
        credentials.setMail("user@example.com");
        credentials.setPassword("wrong-password");

        assertThrows(InvalidCredentialsException.class, () -> authService.authenticate(credentials));
    }
   @Test
    void testCheckLogin_success() {
        User user = mockUser(1L, "encoded", true, null, false);
        Person person = new Person();
        person.setProfileImageUrl("http://image.url");

        Roles role = new Roles();
        role.setId(1L);
        role.setName("ADMIN");

        UserPerRole upr = mock(UserPerRole.class);
        when(upr.getRole()).thenReturn(role);

        when(jwtUtil.isTokenValid("valid-token")).thenReturn(true);
        when(jwtUtil.extractUsername("valid-token")).thenReturn("1");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(personRepository.findByUser(user)).thenReturn(Optional.of(person));
        when(roleRepository.findByUser(user)).thenReturn(List.of(upr));

        ApiResponse<CheckLogin> response = authService.checkLogin("valid-token");

        assertFalse(response.isError());
        assertEquals(200, response.getCode());
        assertEquals("ADMIN", response.getData().getRole());
        assertEquals("http://image.url", response.getData().getProfileImgUrl());
    }

    @Test
    void testCheckLogin_invalidToken() {
        when(jwtUtil.isTokenValid("invalid")).thenReturn(false);

        ApiResponse<CheckLogin> response = authService.checkLogin("invalid");

        assertTrue(response.isError());
        assertEquals(401, response.getCode());
    }

    @Test
    void testCheckLogin_nullToken() {
        ApiResponse<CheckLogin> response = authService.checkLogin(null);

        assertTrue(response.isError());
        assertEquals(401, response.getCode());
    }

    // Utilidades
    private User mockUser(Long id, String password, Boolean verified, LocalDateTime deletedAt, Boolean firstLogin) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        when(user.getPassword()).thenReturn(password);
        when(user.isVerified()).thenReturn(verified);
        when(user.getVerified()).thenReturn(verified);
        when(user.getDeleted_at()).thenReturn(deletedAt);
        when(user.getFirstLogin()).thenReturn(firstLogin);
        return user;
    }
}