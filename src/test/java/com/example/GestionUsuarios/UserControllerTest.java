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
import com.JSCode.GestionUsuarios.dto.ApiResponse;
import com.JSCode.GestionUsuarios.dto.UserRegisterDto;
import com.JSCode.GestionUsuarios.exceptions.BadRequestException;
import com.JSCode.GestionUsuarios.exceptions.ConflictException;
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
        dto.setRoleName("usuario");

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
        dto.setRoleName("usuario");

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
void testSendEmail_Success() {
    String email = "test@example.com";
    when(userService.sendVerificationEmail(email)).thenReturn(true);

    ResponseEntity<ApiResponse<String>> response = userController.sendEmail(email);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("Correo enviado exitosamente", response.getBody().getMessage());
    assertEquals(false, response.getBody().isError());
    verify(userService, times(1)).sendVerificationEmail(email);
}

@Test
void testSendEmail_Failure() {
    String email = "test@example.com";
    when(userService.sendVerificationEmail(email)).thenThrow(new RuntimeException("Error enviando correo"));

    try {
        userController.sendEmail(email);
    } catch (RuntimeException ex) {
        assertEquals("Error enviando correo", ex.getMessage());
    }

    verify(userService, times(1)).sendVerificationEmail(email);
}

@Test
void testValidateCode_Success() {
    VerificationRequest request = new VerificationRequest("test@example.com", "123456");
    when(userService.validateVerificationCode(any(VerificationRequest.class))).thenReturn(true);

    ResponseEntity<ApiResponse<String>> response = userController.validateCode(request);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("Código validado correctamente", response.getBody().getMessage());
    assertEquals(false, response.getBody().isError());
    verify(userService, times(1)).validateVerificationCode(any(VerificationRequest.class));
}

@Test
void testValidateCode_InvalidCode() {
    VerificationRequest request = new VerificationRequest("test@example.com", "wrong-code");
    when(userService.validateVerificationCode(any(VerificationRequest.class)))
            .thenThrow(new BadRequestException("Código inválido"));

    try {
        userController.validateCode(request);
    } catch (BadRequestException ex) {
        assertEquals("Código inválido", ex.getMessage());
    }

    verify(userService, times(1)).validateVerificationCode(any(VerificationRequest.class));
}

}
