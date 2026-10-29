package com.DulceMayor.backend.entities.facturacion;

/**
 * Enumeración de los estados posibles de una factura según {@code 01-dominio.md} y {@code 04-openapi.yaml}.
 */
public enum EstadoFactura {
    PENDIENTE_PAGO,
    PAGADA,
    ANULADA
}

