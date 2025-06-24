package com.JSCode.GestionUsuarios.dto.auth;

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
