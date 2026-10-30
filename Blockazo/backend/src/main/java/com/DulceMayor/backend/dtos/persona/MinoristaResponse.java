package com.DulceMayor.backend.dtos.persona;

import com.DulceMayor.backend.dtos.Identificable;
import com.DulceMayor.backend.dtos.direccion.DireccionResponse;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO de salida para un cliente minorista.
 */
public record MinoristaResponse(
        String id,
        String nombre,
        String apellido,
        LocalDate fechaNacimiento,
        String numeroDocumento,
        List<DireccionResponse> direcciones,
        boolean tieneUsuario
) implements Identificable {
}
