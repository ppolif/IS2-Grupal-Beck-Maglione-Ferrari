package com.DulceMayor.backend.dtos.ruta;

import com.DulceMayor.backend.dtos.Identificable;
import com.DulceMayor.backend.dtos.direccion.DireccionResponse;
import com.DulceMayor.backend.dtos.persona.EmpleadoResumen;
import com.DulceMayor.backend.dtos.persona.PuntoDeVentaResponse;
import com.DulceMayor.backend.entities.ruta.RutaEstado;
import com.DulceMayor.backend.entities.ruta.TipoRuta;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO de respuesta para una ruta (preventor o distribuidor) según {@code 04-openapi.yaml}.
 * Incluye los datos de la sucursal de salida ({@code sucursalOrigenNombre} y {@code sucursalOrigenDireccion})
 * para su representación y trazado vial en OpenStreetMap.
 */
public record RutaResponse(
        String id,
        TipoRuta tipo,
        LocalDate fecha,
        RutaEstado estado,
        PuntoDeVentaResponse paradaActual,
        EmpleadoResumen responsable,
        String sucursalOrigenNombre,
        DireccionResponse sucursalOrigenDireccion,
        List<ParadaResponse> paradas
) implements Identificable {
}

