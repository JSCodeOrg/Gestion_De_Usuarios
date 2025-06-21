package com.JSCode.GestionUsuarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos del usuario que pueden ser editados")
public class EditData {

    @Schema(description = "ID del usuario", example = "12")
    private Long userId;

    @Schema(description = "Nombre del usuario", example = "Carlos")
    private String nombre;

    @Schema(description = "Apellido del usuario", example = "Ruiz")
    private String apellido;

    @Schema(description = "Documento del usuario", example = "1020304050")
    private String document;

    @Schema(description = "Teléfono del usuario", example = "+57 3001234567")
    private String telefono;

    @Schema(description = "Dirección del usuario", example = "Calle 45 #67-89, Bogotá")
    private String direccion;

    @Schema(description = "URL de la foto de perfil del usuario", example = "https://example.com/images/12.jpg")
    private String fotoURL;
}
