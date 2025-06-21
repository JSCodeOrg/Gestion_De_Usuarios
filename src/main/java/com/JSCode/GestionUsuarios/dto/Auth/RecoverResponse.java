package com.JSCode.GestionUsuarios.dto.Auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Respuesta tras validar el código de recuperación")
public class RecoverResponse {
    @Schema(description = "Correo del usuario validado", example = "usuario@example.com")
    private String mail;
    @Schema(description = "Token de recuperación generado para cambiar la contraseña", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String recoveryToken;

    public RecoverResponse(String mail, String recoveryToken){
        this.mail = mail;
        this.recoveryToken = recoveryToken;
    }
}