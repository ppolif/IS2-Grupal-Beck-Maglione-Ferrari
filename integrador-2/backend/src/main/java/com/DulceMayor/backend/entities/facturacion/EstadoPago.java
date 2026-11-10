package com.DulceMayor.backend.entities.facturacion;

/**
 * Estados posibles de un cobro o transacción en el ciclo de pagos.
 */
public enum EstadoPago {
    PENDIENTE,
    APROBADO,
    RECHAZADO,
    DEVUELTO
}
