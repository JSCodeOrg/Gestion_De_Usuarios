package com.JSCode.GestionUsuarios.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EditData {
    
    private Long userId;
    private String nombre;
    private String apellido;
    private String document;
    private String telefono;
    private String direccion;
    
}
