package com.JSCode.GestionUsuarios.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.JSCode.GestionUsuarios.dto.Response;
import com.JSCode.GestionUsuarios.dto.Auth.AuthResponse;
import com.JSCode.GestionUsuarios.dto.Auth.CheckLogin;
import com.JSCode.GestionUsuarios.dto.Auth.UserCredentials;
import com.JSCode.GestionUsuarios.services.AuthService;
import jakarta.servlet.http.HttpServletResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // Login

    @Operation(
    summary = "Iniciar sesión",
    description = "Autentica al usuario con su correo y contraseña. Devuelve un token JWT y rol si las credenciales son válidas."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Inicio de sesión exitoso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(
                    name = "Respuesta exitosa",
                    value = """
                    {
                    "token": "eyJhbGciOiJIUzI1NiJ9...",
                    "role": "cliente",
                    "id": 3
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Credenciales inválidas",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "\"Correo o contraseña incorrectos\""
                )
            )
        )
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
        required = true,
        description = "Credenciales del usuario",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = UserCredentials.class),
            examples = @ExampleObject(
                name = "Login ejemplo",
                value = """
                {
                  "mail": "juan@example.com",
                  "password": "123456"
                }
                """
            )
        )
    )
        
        @RequestBody UserCredentials userCredentials, HttpServletResponse response) {
        System.out.println(userCredentials.getMail());
        System.out.println(userCredentials.getPassword());
        AuthResponse authResponse = authService.authenticate(userCredentials);
        return ResponseEntity.ok(authResponse);
    }

    // me

    @Operation(
    summary = "Validar sesión del usuario",
    description = "Verifica si el token JWT proporcionado es válido y devuelve los datos del usuario."
)
@ApiResponses(value = {
    @ApiResponse(
        responseCode = "200",
        description = "Sesión válida",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = Response.class),
            examples = @ExampleObject(
                name = "Sesión activa",
                value = """
                {
                  "message": "Sesión válida",
                  "data": {
                    "active": true,
                    "role": "cliente",
                    "id": 3
                  },
                  "error": false,
                  "status": 200
                }
                """
            )
        )
    ),
    @ApiResponse(
        responseCode = "401",
        description = "Token inválido o no proporcionado",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Token inválido",
                value = """
                {
                  "message": "Token inválido o expirado",
                  "data": null,
                  "error": true,
                  "status": 401
                }
                """
            )
        )
    )
})
    @GetMapping("/me")
    public ResponseEntity<Response<CheckLogin>> checkLogin(
        @Parameter(
        description = "Token JWT en formato Bearer",
        required = true,
        example = "Bearer eyJhbGciOiJIUzI1NiJ9..."
        )
        @RequestHeader("Authorization") String authorizationHeader) {

        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new Response<>("Token no proporcionado", null, true, 401));
        }

        if (!authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new Response<>("Token en formato incorrecto", null, true, 401));
        }

        String token = authorizationHeader.substring(7); 

        Response<CheckLogin> verifySession = authService.checkLogin(token);
        if (verifySession.isError()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new Response<>("Token inválido o expirado", null, true, 401));
        }

        return ResponseEntity.ok(verifySession);
    }
}
