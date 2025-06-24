package com.JSCode.GestionUsuarios.dto.users;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VerificationEditionRequest {
    private Long id;
    private String password;

}
