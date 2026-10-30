package com.DulceMayor.backend.config;

public class ConflictoException extends RuntimeException {

    private final String code;

    public ConflictoException(String message) {
        super(message);
        this.code = "CONFLICTO";
    }

    public ConflictoException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

