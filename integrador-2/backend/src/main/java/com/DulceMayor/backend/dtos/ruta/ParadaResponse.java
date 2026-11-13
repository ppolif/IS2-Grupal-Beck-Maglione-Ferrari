package com.DulceMayor.backend.dtos.ruta;

import com.DulceMayor.backend.dtos.persona.PuntoDeVentaResponse;
import com.DulceMayor.backend.entities.facturacion.TipoPago;

/**
 * DTO que representa una parada dentro de la secuencia proyectada de una ruta según {@code 04-openapi.yaml}.
 *
 * <p>Es una vista calculada para el cliente; no corresponde a una entidad de base de datos propia,
 * manteniendo el modelo relacional simple. En rutas de distribución incluye los datos financieros
 * del pedido ({@code numeroPedido}, {@code totalPedido}, {@code tipoPago} y {@code esEfectivo})
 * para gestionar el cobro y la recaudación en efectivo contra entrega.</p>
 */
public record ParadaResponse(
        int orden,
        boolean completada,
        PuntoDeVentaResponse puntoDeVenta,
        String pedidoId,
        Long numeroPedido,
        Double totalPedido,
        TipoPago tipoPago,
        boolean esEfectivo
) {
    public ParadaResponse(
            int orden,
            boolean completada,
            PuntoDeVentaResponse puntoDeVenta,
            String pedidoId) {
        this(orden, completada, puntoDeVenta, pedidoId, null, null, null, false);
    }
}

