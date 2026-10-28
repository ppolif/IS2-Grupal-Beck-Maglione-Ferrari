package com.DulceMayor.backend.dtos;

import java.time.LocalDateTime;

/**
 * DTO de salida que representa el stock de un producto o movimiento registrado según 04-openapi.yaml.
 *
 * <p><b>Patrón GoF relacionado: DTO / Adapter</b></p>
 * Representa el DTO inmutable (Record de Java) proyectado a partir de la entidad JPA {@code Stock}.
 */
public record StockResponse(
        String id,
        String productoId,
        String productoNombre,
        Integer cantidadActual,
        String observacion,
        LocalDateTime fechaHora
) {
}
