package com.DulceMayor.backend.dtos;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Formato único de respuesta de error según el contrato OpenAPI.
 */
public record ErrorResponse(
        String timestamp,
        int status,
        String code,
        String message,
        String path,
        List<DetalleCampoError> errors
) {

    public record DetalleCampoError(
            String campo,
            String mensaje
    ) {
    }

    public static ErrorResponse of(int status, String code, String message, String path) {
        return new ErrorResponse(
                OffsetDateTime.now().toString(),
                status,
                code,
                message,
                path,
                List.of()
        );
    }

    public static ErrorResponse of(int status, String code, String message, String path, List<DetalleCampoError> errors) {
        return new ErrorResponse(
                OffsetDateTime.now().toString(),
                status,
                code,
                message,
                path,
                errors != null ? errors : List.of()
        );
    }
}

