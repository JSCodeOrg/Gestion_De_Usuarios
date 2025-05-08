package com.JSCode.GestionUsuarios.dto.Auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckLogin {
    private String token;
    private Long user_id;
    private String profileImgUrl;
    private String role;
}
