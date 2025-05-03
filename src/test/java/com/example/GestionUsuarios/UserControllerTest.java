package com.example.GestionUsuarios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import com.JSCode.GestionUsuarios.controllers.UserController;
import com.JSCode.GestionUsuarios.dto.ApiResponse;
import com.JSCode.GestionUsuarios.dto.VerificationEditionRequest;
import com.JSCode.GestionUsuarios.dto.register.UserRegisterDto;
import com.JSCode.GestionUsuarios.exceptions.BadRequestException;
import com.JSCode.GestionUsuarios.exceptions.ConflictException;
import com.JSCode.GestionUsuarios.exceptions.NotFoundException;
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
    void testRegisterUser_Success() {
        UserRegisterDto dto = new UserRegisterDto();
        dto.setMail("example@mail.com");
        dto.setPassword("password");
        dto.setDocument("12345678");
        dto.setNombre("Juan");
        dto.setApellido("Perez");
        dto.setDireccion("Calle Falsa 123");
        dto.setTelefono("9384982934");

        User mockUser = new User();
        mockUser.setMail(dto.getMail());

        when(userService.registerUser(any(UserRegisterDto.class))).thenReturn(mockUser);

        ResponseEntity<ApiResponse<User>> response = userController.register(dto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Registro exitoso", response.getBody().getMessage());
        assertEquals(mockUser, response.getBody().getData());
        assertEquals(false, response.getBody().isError());
        assertEquals(200, response.getBody().getStatus());

        verify(userService, times(1)).registerUser(any(UserRegisterDto.class));
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() {
        UserRegisterDto dto = new UserRegisterDto();
        dto.setMail("existing@example.com");

        when(userService.registerUser(any(UserRegisterDto.class)))
                .thenThrow(new ConflictException("El email ya está registrado"));

        try {
            userController.register(dto);
        } catch (ConflictException ex) {
            assertEquals("El email ya está registrado", ex.getMessage());
        }

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

        when(userService.registerUser(any(UserRegisterDto.class)))
                .thenThrow(new BadRequestException("El email no es válido"));

        try {
            userController.register(dto);
        } catch (BadRequestException ex) {
            assertEquals("El email no es válido", ex.getMessage());
        }

        verify(userService, times(1)).registerUser(any(UserRegisterDto.class));
    }

    @Test
    void testVerifyEdition_Success() {
        VerificationEditionRequest request = new VerificationEditionRequest();
        request.setId(1L);
        request.setPassword("password123");
        User mockUser = new User();
        mockUser.setId(1L);

        when(userService.verifyUserEdit(1L, "password123")).thenReturn(mockUser);

        ResponseEntity<ApiResponse<User>> response = userController.verifyEdition(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Usuario verificado correctamente", response.getBody().getMessage());
        assertEquals(false, response.getBody().isError());
        assertEquals(200, response.getBody().getStatus());

        verify(userService, times(1)).verifyUserEdit(1L, "password123");
    }
    @Test
    void testVerifyEdition_MissingFields() {
        VerificationEditionRequest request = new VerificationEditionRequest();
        request.setId(null); // Simulando que no se envió el ID
        request.setPassword(null); // Simulando que no se envió la contraseña
        when(userService.verifyUserEdit(null, null)).thenThrow(new BadRequestException("Se requiere user_id y password"));

        ResponseEntity<ApiResponse<User>> response = userController.verifyEdition(request);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Se requiere user_id y password", response.getBody().getMessage());
        assertEquals(true, response.getBody().isError());
        assertEquals(400, response.getBody().getStatus());

        verifyNoInteractions(userService);
    }

    @Test
    void testVerifyEdition_IncorrectPassword() {
        VerificationEditionRequest request = new VerificationEditionRequest();
        request.setId(1L);
        request.setPassword("wrongPassword");

        when(userService.verifyUserEdit(1L, "wrongPassword")).thenThrow(new BadRequestException("Contraseña incorrecta"));

        try {
            userController.verifyEdition(request);
        } catch (BadRequestException ex) {
            assertEquals("Contraseña incorrecta", ex.getMessage());
        }

        verify(userService, times(1)).verifyUserEdit(1L, "wrongPassword");
    }

    @Test
    void testVerifyEdition_UserNotFound() {
        VerificationEditionRequest request = new VerificationEditionRequest();
        request.setId(99L);
        request.setPassword("password123");

        when(userService.verifyUserEdit(99L, "password123")).thenThrow(new NotFoundException("Usuario no encontrado"));

        try {
            userController.verifyEdition(request);
        } catch (NotFoundException ex) {
            assertEquals("Usuario no encontrado", ex.getMessage());
        }

        verify(userService, times(1)).verifyUserEdit(99L, "password123");
    }
}
