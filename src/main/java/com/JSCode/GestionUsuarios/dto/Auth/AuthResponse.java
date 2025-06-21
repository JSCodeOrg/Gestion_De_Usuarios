package com.JSCode.GestionUsuarios.dto.Auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Respuesta después de autenticar al usuario")
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
