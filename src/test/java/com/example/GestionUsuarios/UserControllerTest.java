package com.example.GestionUsuarios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.JSCode.GestionUsuarios.controllers.UserController;
import com.JSCode.GestionUsuarios.dto.UserRegisterDto;
import com.JSCode.GestionUsuarios.models.User;
import com.JSCode.GestionUsuarios.services.UserService;

class UserControllerTest {
    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    void testRegisterUser_EmailAlreadyExists() {
        UserRegisterDto dto = new UserRegisterDto();
        dto.setMail("existing@example.com");

        when(userService.registerUser(any(UserRegisterDto.class)))
                .thenThrow(new RuntimeException("Email already exists"));

        ResponseEntity<?> response = userController.register(dto);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Email already exists", response.getBody());

        verify(userService, times(1)).registerUser(any(UserRegisterDto.class));
    }
    @Test
    void testRegisterUser_InvalidEmail() {
        UserRegisterDto dto = new UserRegisterDto();
        dto.setMail("email-sin-arroba");
        dto.setPassword("password");
        dto.setDocument("12345678");
        dto.setNombre("Juan");
        dto.setApellido("Perez");
        dto.setDireccion("Calle Falsa 123");
        dto.setTelefono("9384982934");
        dto.setRoleName("usuario");

        when(userService.registerUser(any(UserRegisterDto.class)))
                .thenThrow(new RuntimeException("Invalid Email"));

        ResponseEntity<?> response = userController.register(dto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()); 
        assertEquals("Invalid Email", response.getBody()); 
        verify(userService, times(1)).registerUser(any(UserRegisterDto.class));
    }
}