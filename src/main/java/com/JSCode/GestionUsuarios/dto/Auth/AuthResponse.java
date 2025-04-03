package com.JSCode.GestionUsuarios.dto.Auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponse {

    private String token;
    private String name;
    private String email;
    private Long user_id;
    private Boolean firstLogin;


    public AuthResponse(String token, String name, String email, Long user_id, boolean firstLogin) {
        this.token = token;
        this.name = name;
        this.email = email;
        this.user_id = user_id;
        this.firstLogin = firstLogin;
    }
    
}
