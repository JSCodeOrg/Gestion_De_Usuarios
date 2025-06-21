package com.JSCode.GestionUsuarios.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Schema(description = "Solicitud para desactivar una cuenta de usuario")
public class DeactivationRequest {

    @Schema(description = "Correo electrónico del usuario a desactivar", example = "usuario@example.com", required = true)
    private String mail;
}