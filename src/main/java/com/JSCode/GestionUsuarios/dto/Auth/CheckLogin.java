package com.JSCode.GestionUsuarios.dto.Auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Respuesta al validar sesión activa")
public class CheckLogin {
    @Schema(
        description = "Token de sesión activa del usuario",
        example = "eyJhbGciOiJIUzI1NiJ9..."
    )
    private String token;

    @Schema(
        description = "ID del usuario autenticado",
        example = "42"
    )
    private Long user_id;

    @Schema(
        description = "URL de la imagen de perfil del usuario",
        example = "https://example.com/images/profile42.png"
    )
    private String profileImgUrl;

    @Schema(
        description = "Rol del usuario",
        example = "administrador"
    )
    private String role;
}
