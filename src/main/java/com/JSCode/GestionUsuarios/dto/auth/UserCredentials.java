package com.JSCode.GestionUsuarios.dto.auth;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserCredentials {
    private String mail;
    private String password;
}