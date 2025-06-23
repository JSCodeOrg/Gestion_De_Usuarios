package com.JSCode.GestionUsuarios.dto.register;

import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@Schema(description = "Datos editables de un usuario")
public class EditDataDTO {

    @Schema(description = "Nombre del usuario", example = "Juan")
    private String nombre;

    @Schema(description = "Apellido del usuario", example = "Pérez")
    private String apellido;
    @Schema(description = "Documento del usuario", example = "Pérez")
    private String document;
    @Schema(description = "Direccion del usuario", example = "Pérez")
    private String direccion;

    @Schema(description = "Teléfono de contacto", example = "3001234567")
    private String telefono;

    @Schema(description = "Número de documento", example = "1020304050")
    private String documento;

    @Schema(description = "Correo electrónico", example = "juan.perez@example.com")
    private String email;
}
