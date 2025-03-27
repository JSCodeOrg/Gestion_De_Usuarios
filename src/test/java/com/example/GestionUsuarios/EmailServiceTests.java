package com.example.GestionUsuarios;

import org.junit.jupiter.api.Test;
import com.JSCode.GestionUsuarios.controllers.UserController;
import com.JSCode.GestionUsuarios.dto.ApiResponse;
import com.JSCode.GestionUsuarios.dto.VerificationRequest;
import com.JSCode.GestionUsuarios.services.UserService;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class EmailServiceTests {

    @Mock
    private UserService userService; // Mock del servicio de usuario

    @InjectMocks
    private UserController userController; // Controlador que usa el servicio

    @Test
    void testVerifyUser_Success() {

        String email = "test@example.com";
        String verificationCode = "123456";
        VerificationRequest request = new VerificationRequest(email, verificationCode);


        when(userService.verifyUser(email, verificationCode)).thenReturn(true);


        ResponseEntity<ApiResponse<Void>> response = userController.verifyUser(request);


        assertEquals(200, response.getStatusCode().value());
        assertEquals("Email verificado correctamente", response.getBody().getMessage());
        assertFalse(response.getBody().isError());
        assertEquals(200, response.getBody().getStatus());

        verify(userService, times(1)).verifyUser(email, verificationCode);
    }

    @Test
    void testVerifyUser_Failure() {

        String email = "test@example.com";
        String verificationCode = "wrong_code";
        VerificationRequest request = new VerificationRequest(email, verificationCode);

        when(userService.verifyUser(email, verificationCode)).thenReturn(false);

        ResponseEntity<ApiResponse<Void>> response = userController.verifyUser(request);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Codigo de verificacion invalido", response.getBody().getMessage());
        assertTrue(response.getBody().isError());
        assertEquals(400, response.getBody().getStatus());

        verify(userService, times(1)).verifyUser(email, verificationCode);
    }
}