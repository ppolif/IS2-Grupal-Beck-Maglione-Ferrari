package com.example.tinder.dto;

import java.util.Date;

public class ApiResponseDto<T> {
    private boolean exito;
    private String mensaje;
    private T datos;
    private Date timestamp;

    public ApiResponseDto() {
        this.timestamp = new Date();
    }

    public ApiResponseDto(boolean exito, String mensaje) {
        this.exito = exito;
        this.mensaje = mensaje;
        this.timestamp = new Date();
    }

    public ApiResponseDto(boolean exito, String mensaje, T datos) {
        this.exito = exito;
        this.mensaje = mensaje;
        this.datos = datos;
        this.timestamp = new Date();
    }

    public static <T> ApiResponseDto<T> ok(String mensaje, T datos) {
        return new ApiResponseDto<>(true, mensaje, datos);
    }

    public static <T> ApiResponseDto<T> ok(String mensaje) {
        return new ApiResponseDto<>(true, mensaje, null);
    }

    public static <T> ApiResponseDto<T> error(String mensaje) {
        return new ApiResponseDto<>(false, mensaje, null);
    }

    public boolean isExito() {
        return exito;
    }

    public void setExito(boolean exito) {
        this.exito = exito;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public T getDatos() {
        return datos;
    }

    public void setDatos(T datos) {
        this.datos = datos;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}

