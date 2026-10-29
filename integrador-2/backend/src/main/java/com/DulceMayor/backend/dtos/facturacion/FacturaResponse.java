package com.DulceMayor.backend.dtos.facturacion;

import com.DulceMayor.backend.dtos.Identificable;
import com.DulceMayor.backend.entities.facturacion.EstadoFactura;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de salida que representa una factura completa con sus detalles, pagos y saldo según {@code 04-openapi.yaml}.
 */
public record FacturaResponse(
        String id,
        Long numeroFactura,
        LocalDateTime fechaFactura,
        String pedidoId,
        EstadoFactura estado,
        Double total,
        Double totalPagado,
        Double saldo,
        List<DetalleFacturaResponse> detalles,
        List<PagoResponse> pagos
) implements Identificable {
}

