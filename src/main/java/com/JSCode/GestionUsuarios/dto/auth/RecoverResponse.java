package com.JSCode.GestionUsuarios.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecoverResponse {
    private String mail;
    private String recoveryToken;

    public RecoverResponse(String mail, String recoveryToken){
        this.mail = mail;
        this.recoveryToken = recoveryToken;
    }
}