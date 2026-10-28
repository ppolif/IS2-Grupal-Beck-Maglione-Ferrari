package com.DulceMayor.backend.config;

public class RecursoNoEncontradoException extends RuntimeException {

    private final String code;

    public RecursoNoEncontradoException(String message) {
        super(message);
        this.code = "NO_ENCONTRADO";
    }

    public String getCode() {
        return code;
    }
}

