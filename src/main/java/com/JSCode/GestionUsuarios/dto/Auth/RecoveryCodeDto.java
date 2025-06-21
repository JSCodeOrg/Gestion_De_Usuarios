package com.JSCode.GestionUsuarios.dto.Auth;

import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@Schema(description = "Datos para verificar el código de recuperación")
public class RecoveryCodeDto {

    @Schema(description = "Correo electrónico del usuario", example = "usuario@example.com", required = true)
    private String mail;

    @Schema(description = "Código de verificación enviado al correo", example = "123456", required = true)
    private String code;
}
