package com.JSCode.GestionUsuarios.dto.users;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDataDTO extends CommonUserFieldsDTO {
    private String fotoperfil;
    private String documento;
    private String email;
}
