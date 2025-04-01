package com.JSCode.GestionUsuarios.dto.Auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecoveryCodeDto {
    private String mail;
    private String code;
    
}
