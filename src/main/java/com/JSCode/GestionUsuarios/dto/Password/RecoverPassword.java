package com.JSCode.GestionUsuarios.dto.Password;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud para recuperación de contraseña")
public class RecoverPassword {
    @Schema(description = "Correo electrónico del usuario registrado", example = "usuario@example.com", required = true)
    private String mail; 
}
