package com.JSCode.GestionUsuarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Datos del perfil del usuario autenticado")
public class UserDataDTO {

    @Schema(
        description = "URL de la foto de perfil del usuario",
        example = "https://example.com/images/profile.jpg"
    )
    private String fotoperfil;

    @Schema(
        description = "Nombre del usuario",
        example = "Carlos"
    )
    private String nombre;

    @Schema(
        description = "Apellido del usuario",
        example = "Ruiz"
    )
    private String apellido;

    @Schema(
        description = "Número de documento del usuario",
        example = "1234567890"
    )
    private String documento;

    @Schema(
        description = "Correo electrónico del usuario",
        example = "carlos@example.com"
    )
    private String email;

    @Schema(
        description = "Número de teléfono del usuario",
        example = "+57 3001234567"
    )
    private String telefono;

    @Schema(
        description = "Dirección del usuario",
        example = "Cra. 10 #15-20, Medellín"
    )
    private String direccion;
}
