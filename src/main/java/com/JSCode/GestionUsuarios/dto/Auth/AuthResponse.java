package com.JSCode.GestionUsuarios.dto.Auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponse {

    private String token;
    private Long user_id;
    private Boolean firstLogin;


    public AuthResponse(String token, Long user_id, boolean firstLogin) {
        this.token = token;
        this.user_id = user_id;
        this.firstLogin = firstLogin;
    }
    
}
