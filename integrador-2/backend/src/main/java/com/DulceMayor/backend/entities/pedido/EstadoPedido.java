package com.DulceMayor.backend.entities.pedido;

/**
 * Estados posibles del ciclo de vida de un {@link Pedido} según {@code 01-dominio.md} y {@code 02-reglas-negocio.md}.
 */
public enum EstadoPedido {
    CARGADO,
    SIN_STOCK,
    FACTURADO,
    EN_REPARTO,
    ENTREGADO,
    ANULADO
}

