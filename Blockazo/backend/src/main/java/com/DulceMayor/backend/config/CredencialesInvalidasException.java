package com.DulceMayor.backend.config;

public class CredencialesInvalidasException extends RuntimeException {

    public static final String CODIGO = "CREDENCIALES_INVALIDAS";
    public static final String MENSAJE = "Usuario o clave inválidos";

    public CredencialesInvalidasException() {
        super(MENSAJE);
    }

    public String getCode() {
        return CODIGO;
    }
}

