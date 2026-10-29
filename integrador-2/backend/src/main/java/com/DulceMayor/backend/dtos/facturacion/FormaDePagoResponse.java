package com.DulceMayor.backend.dtos.facturacion;

import com.DulceMayor.backend.dtos.Identificable;
import com.DulceMayor.backend.entities.facturacion.TipoPago;

/**
 * DTO de salida para una forma de pago activa según {@code 04-openapi.yaml}.
 */
public record FormaDePagoResponse(
        String id,
        TipoPago tipoPago,
        String observacion
) implements Identificable {
}

