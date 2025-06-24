package com.JSCode.GestionUsuarios.dto.users;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EditData extends CommonUserFieldsDTO {

    private Long userId;
    private String document;   
    private String fotoURL;  
}
