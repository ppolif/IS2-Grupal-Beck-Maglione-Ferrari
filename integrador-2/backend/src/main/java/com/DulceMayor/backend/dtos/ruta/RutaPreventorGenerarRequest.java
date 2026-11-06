package com.DulceMayor.backend.dtos.ruta;

import java.time.LocalDate;

/**
 * DTO para solicitar la generación de la ruta diaria del preventor.
 * No requiere coordenadas ya que el sistema toma automáticamente la ubicación
 * de la sucursal a la que pertenece el preventor.
 */
public record RutaPreventorGenerarRequest(
        String preventorId,
        LocalDate fecha
) {
}

