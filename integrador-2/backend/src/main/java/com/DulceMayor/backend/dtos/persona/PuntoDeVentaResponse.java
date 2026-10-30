package com.DulceMayor.backend.dtos.persona;

import com.DulceMayor.backend.dtos.Identificable;
import com.DulceMayor.backend.dtos.direccion.DireccionResponse;

/**
 * DTO de salida para un punto de venta según {@code 04-openapi.yaml}.
 */
public record PuntoDeVentaResponse(
        String id,
        String nombreComercio,
        MinoristaResumen minorista,
        DireccionResponse direccion,
        EmpleadoResumen preventor
) implements Identificable {
}

