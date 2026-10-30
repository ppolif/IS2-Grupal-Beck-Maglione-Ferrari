package com.DulceMayor.backend.config;

public class ValidacionException extends RuntimeException {

    private final String code;

    public ValidacionException(String message) {
        super(message);
        this.code = "VALIDACION";
    }

    public ValidacionException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

