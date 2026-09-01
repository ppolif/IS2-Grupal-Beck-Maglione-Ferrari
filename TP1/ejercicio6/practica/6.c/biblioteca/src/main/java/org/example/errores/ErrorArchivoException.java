package org.example.errores;

public class ErrorArchivoException extends RuntimeException {
    public ErrorArchivoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}