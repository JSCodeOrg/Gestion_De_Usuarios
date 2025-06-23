package com.JSCode.GestionUsuarios.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponse<T> {
    private String message;
    private T data;
    private boolean error;
    private int status;
 
    public ApiResponse(String message, T data, boolean error, int status){
        this.message = message;
        this.data = data;
        this.error = error;
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public int getCode() {
        return status;
    }

    public ApiResponse(String message, boolean error, int status) {
        this(message, null, error, status);
    }

}
