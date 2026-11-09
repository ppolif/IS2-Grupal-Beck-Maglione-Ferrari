package com.DulceMayor.backend.dtos.publico;

import java.time.LocalDateTime;

/**
 * DTO de salida con la cotización del dólar según {@code 04-openapi.yaml}.
 */
public record CotizacionDolarResponse(
        String moneda,
        Double compra,
        Double venta,
        LocalDateTime fechaActualizacion,
        String fuente,
        Boolean esValorRespaldo
) {
}

