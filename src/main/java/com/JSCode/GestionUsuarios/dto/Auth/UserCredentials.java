package com.JSCode.GestionUsuarios.dto.Auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Credenciales del usuario para autenticación")
public class UserCredentials {
    @Schema(description = "Correo del usuario", example = "juan@example.com")
    private String mail;

    @Schema(description = "Contraseña del usuario", example = "123456")
    private String password;
}