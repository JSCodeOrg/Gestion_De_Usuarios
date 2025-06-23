package com.JSCode.GestionUsuarios.dto.Auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecoverResponse {
    private String mail;
    private String recoveryToken;
    private String token;

    public RecoverResponse(String mail, String recoveryToken, String token){
        this.mail = mail;
        this.recoveryToken = recoveryToken;
        this.token = token;
    }

    public String getMail() {
        return mail;
    }

    public String getToken() {
        return token;
    }
}