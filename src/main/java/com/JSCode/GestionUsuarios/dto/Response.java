package com.JSCode.GestionUsuarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Formato estándar de respuesta del servidor")
public class Response<T> {

    @Schema(
        description = "Mensaje de la operación realizada",
        example = "Operación exitosa"
    )
    private String message;

    @Schema(
        description = "Cuerpo de la respuesta con los datos solicitados. Puede ser null",
        nullable = true
    )
    private T data;

    @Schema(
        description = "Indica si hubo error en la operación",
        example = "false"
    )
    private boolean error;

    @Schema(
        description = "Código de estado HTTP de la operación",
        example = "200"
    )
    private int status;
 
    public Response(String message, T data, boolean error, int status){
        this.message = message;
        this.data = data;
        this.error = error;
        this.status = status;
    }

    public Response(String message, boolean error, int status) {
        this(message, null, error, status);
    }

}
