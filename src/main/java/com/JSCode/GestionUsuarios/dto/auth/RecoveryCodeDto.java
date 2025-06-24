package com.JSCode.GestionUsuarios.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecoveryCodeDto {
    private String mail;
    private String code;
    
}
