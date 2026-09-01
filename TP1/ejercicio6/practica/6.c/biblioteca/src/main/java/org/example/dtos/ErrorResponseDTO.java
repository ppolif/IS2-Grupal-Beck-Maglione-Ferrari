package org.example.dtos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ErrorResponseDTO {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    // Opcional: Un código de seguimiento para buscar en logs, mitigando exponer detalles en el mensaje
    private String traceId;
}