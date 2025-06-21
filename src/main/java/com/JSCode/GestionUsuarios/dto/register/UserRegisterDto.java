package com.JSCode.GestionUsuarios.dto.register;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@Schema(description = "Datos necesarios para registrar un nuevo usuario")
public class UserRegisterDto {

    @NotBlank
    @Schema(description = "Correo electrónico del usuario", example = "juan.perez@example.com")
    private String mail;

    @NotBlank
    @Schema(description = "Contraseña del usuario (mínimo 8 caracteres, con mayúsculas, minúsculas y números)", example = "Contrasena123!")
    private String password;

    @NotBlank
    @Schema(description = "Número de documento del usuario", example = "123456789")
    private String document;

    @NotBlank
    @Schema(description = "Nombres del usuario", example = "Juan")
    private String nombre;

    @NotBlank
    @Schema(description = "Apellidos del usuario", example = "Pérez Gómez")
    private String apellido;

    @NotBlank
    @Schema(description = "Dirección de residencia", example = "Cra 45 #10-23, Bogotá")
    private String direccion;

    @NotBlank
    @Schema(description = "Número de teléfono", example = "+57 3201234567")
    private String telefono;

    @Schema(description = "URL de la imagen de perfil (opcional)", example = "https://example.com/profile.jpg")
    private String profileImageUrl;
}
