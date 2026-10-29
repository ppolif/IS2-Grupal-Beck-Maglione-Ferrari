package com.DulceMayor.backend.dtos.facturacion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * DTO de entrada para la generación transaccional de una factura y sus detalles.
 */
public record FacturaRequest(
        String pedidoId,

        @NotEmpty(message = "La factura debe contener al menos un detalle")
        List<@Valid DetalleFacturaRequest> detalles,

        Double descuento
) {
}

