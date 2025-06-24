package com.example.GestionUsuarios;

import org.junit.jupiter.api.Test;
import com.JSCode.GestionUsuarios.controller.UserController;
import com.JSCode.GestionUsuarios.dto.ApiResponse;
import com.JSCode.GestionUsuarios.dto.auth.VerificationRequest;
import com.JSCode.GestionUsuarios.service.UserService;
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
    private UserService userService; 

    @InjectMocks
    private UserController userController; 
    

    //TODO: Como cambiamos la lógica de la verificación de cuenta, toca modificar estas pruebas
}
