package com.DulceMayor.backend.dtos.facturacion;

import com.DulceMayor.backend.entities.facturacion.EstadoPago;
import com.DulceMayor.backend.entities.facturacion.TipoPago;
import java.time.LocalDateTime;

/**
 * Detalle completo de un pago para dashboards administrativos y consulta de clientes.
 */
public record PagoDetalleResponse(
        String id,
        String pedidoId,
        Long numeroPedido,
        String facturaId,
        Long numeroFactura,
        String puntoDeVentaNombre,
        String minoristaNombre,
        TipoPago tipoPago,
        String formaDePagoNombre,
        Double monto,
        EstadoPago estadoPago,
        LocalDateTime fechaPago,
        String referencia,
        String mpPaymentId,
        String mpStatus,
        String observacion,
        String usuarioAuditor
) {
}
