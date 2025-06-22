package com.JSCode.GestionUsuarios.dto.register;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegisterDto {
    @NotBlank
    private String mail;

    @NotBlank
    private String password;

    @NotBlank
    private String document;

    @NotBlank
    private String nombre;

    @NotBlank
    private String apellido;

    @NotBlank
    private String direccion;

    @NotBlank
    private String ciudad;

    @NotBlank
    private String telefono;

    private String profileImageUrl;

}
    
