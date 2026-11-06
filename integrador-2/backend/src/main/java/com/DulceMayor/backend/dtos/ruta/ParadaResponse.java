package com.DulceMayor.backend.dtos.ruta;

import com.DulceMayor.backend.dtos.persona.PuntoDeVentaResponse;

/**
 * DTO que representa una parada dentro de la secuencia proyectada de una ruta según {@code 04-openapi.yaml}.
 *
 * <p>Es una vista calculada para el cliente; no corresponde a una entidad de base de datos propia,
 * manteniendo el modelo relacional simple.</p>
 */
public record ParadaResponse(
        int orden,
        boolean completada,
        PuntoDeVentaResponse puntoDeVenta,
        String pedidoId
) {
}

