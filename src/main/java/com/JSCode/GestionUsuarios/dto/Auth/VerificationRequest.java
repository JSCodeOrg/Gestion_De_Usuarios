package com.JSCode.GestionUsuarios.dto.Auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Petición para verificar una acción sensible usando un token de usuario")
public class VerificationRequest {

    @Schema(
        description = "Token de verificación emitido al usuario",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    private String userToken;
}