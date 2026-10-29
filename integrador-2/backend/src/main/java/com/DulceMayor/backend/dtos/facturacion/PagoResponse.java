package com.DulceMayor.backend.dtos.facturacion;

import com.DulceMayor.backend.dtos.Identificable;
import java.time.LocalDateTime;

/**
 * DTO de salida para un pago registrado según {@code 04-openapi.yaml}.
 */
public record PagoResponse(
        String id,
        String facturaId,
        FormaDePagoResponse formaDePago,
        Double monto,
        LocalDateTime fechaPago,
        String referencia,
        String observacion
) implements Identificable {
}

