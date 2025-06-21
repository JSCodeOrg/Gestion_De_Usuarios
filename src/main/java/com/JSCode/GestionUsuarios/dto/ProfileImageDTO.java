package com.JSCode.GestionUsuarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Respuesta con la URL de la imagen de perfil actualizada")
public class ProfileImageDTO {

    @Schema(
        description = "URL de la nueva imagen de perfil del usuario",
        example = "https://example.com/images/profile123.jpg"
    )
    private String imageUrl;
}
