package com.DulceMayor.backend.dtos.persona;

import com.DulceMayor.backend.dtos.ContactoResponse;
import com.DulceMayor.backend.dtos.Identificable;
import com.DulceMayor.backend.dtos.direccion.DireccionResponse;
import com.DulceMayor.backend.entities.persona.TipoDocumento;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO de salida para un cliente minorista según {@code 04-openapi.yaml}.
 */
public record MinoristaResponse(
        String id,
        String nombre,
        String apellido,
        LocalDate fechaNacimiento,
        TipoDocumento tipoDocumento,
        String numeroDocumento,
        List<DireccionResponse> direcciones,
        List<ContactoResponse> contactos,
        boolean tieneUsuario
) implements Identificable {
}

