package com.DulceMayor.backend.config;

public class AccesoDenegadoException extends RuntimeException {

    private final String code;

    public AccesoDenegadoException(String message) {
        super(message);
        this.code = "ACCESO_DENEGADO";
    }

    public AccesoDenegadoException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

