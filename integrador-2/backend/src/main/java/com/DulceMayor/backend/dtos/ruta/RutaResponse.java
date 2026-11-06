package com.DulceMayor.backend.dtos.ruta;

import com.DulceMayor.backend.dtos.Identificable;
import com.DulceMayor.backend.dtos.persona.EmpleadoResumen;
import com.DulceMayor.backend.dtos.persona.PuntoDeVentaResponse;
import com.DulceMayor.backend.entities.ruta.RutaEstado;
import com.DulceMayor.backend.entities.ruta.TipoRuta;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO de respuesta para una ruta (preventor o distribuidor) según {@code 04-openapi.yaml}.
 */
public record RutaResponse(
        String id,
        TipoRuta tipo,
        LocalDate fecha,
        RutaEstado estado,
        PuntoDeVentaResponse paradaActual,
        EmpleadoResumen responsable,
        List<ParadaResponse> paradas
) implements Identificable {
}

