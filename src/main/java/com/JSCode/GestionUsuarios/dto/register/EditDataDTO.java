package com.JSCode.GestionUsuarios.dto.register;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditDataDTO {
    private String nombre;
    private String apellido;
    private String document;
    private String direccion;
    private String telefono;
    private String password;
}
