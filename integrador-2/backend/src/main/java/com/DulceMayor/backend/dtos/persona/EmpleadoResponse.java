package com.DulceMayor.backend.dtos.persona;

import com.DulceMayor.backend.dtos.ContactoResponse;
import com.DulceMayor.backend.dtos.Identificable;
import com.DulceMayor.backend.dtos.UsuarioResponse;
import com.DulceMayor.backend.dtos.direccion.DireccionResponse;
import com.DulceMayor.backend.entities.persona.TipoDocumento;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO de salida para un empleado según {@code 04-openapi.yaml}.
 */
public record EmpleadoResponse(
        String id,
        String legajo,
        String nombre,
        String apellido,
        LocalDate fechaNacimiento,
        TipoDocumento tipoDocumento,
        String numeroDocumento,
        List<DireccionResponse> direcciones,
        List<ContactoResponse> contactos,
        UsuarioResponse usuario
) implements Identificable {
}

