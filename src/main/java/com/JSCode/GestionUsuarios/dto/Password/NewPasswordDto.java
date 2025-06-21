package com.JSCode.GestionUsuarios.dto.Password;

import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@Schema(description = "Datos necesarios para establecer una nueva contraseña")
public class NewPasswordDto {

    @Schema(description = "Nueva contraseña para el usuario", example = "NuevaPassword123!", required = true)
    private String newPassword;
}