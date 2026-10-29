package com.DulceMayor.backend.dtos.facturacion;

import com.DulceMayor.backend.dtos.Identificable;

/**
 * DTO de salida para una línea de detalle de factura según {@code 04-openapi.yaml}.
 */
public record DetalleFacturaResponse(
        String id,
        String productoId,
        String productoNombre,
        Integer cantidad,
        Double precioUnitario,
        Double subtotal
) implements Identificable {
}

