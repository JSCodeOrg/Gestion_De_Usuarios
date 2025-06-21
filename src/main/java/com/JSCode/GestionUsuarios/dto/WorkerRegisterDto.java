package com.JSCode.GestionUsuarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Datos para registrar un nuevo trabajador")
public class WorkerRegisterDto {
    @Schema(description = "Correo electrónico del trabajador", example = "empleado@example.com", required = true)
    private String email;

    @Schema(description = "ID del rol asignado al trabajador", example = "2", required = true)
    private Long role_id;
}
