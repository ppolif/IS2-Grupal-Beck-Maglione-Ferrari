package com.DulceMayor.backend.dtos.ruta;

import java.time.LocalDate;

/**
 * Solicitud para generar automáticamente las rutas de distribución diaria para los distribuidores activos.
 */
public record RutaDistribuidorRequest(
        LocalDate fecha
) {
}

