package com.JSCode.GestionUsuarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Datos para verificar si el usuario puede editar su información")
public class VerificationEditionRequest {
    @Schema(description = "ID del usuario", example = "123", required = true)
    private Long id;

    @Schema(description = "Contraseña actual del usuario", example = "miContrasena123", required = true)
    private String password;

}
