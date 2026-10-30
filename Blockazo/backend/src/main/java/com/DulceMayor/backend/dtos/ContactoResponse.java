package com.DulceMayor.backend.dtos;

import com.DulceMayor.backend.entities.contacto.TipoContacto;
import com.DulceMayor.backend.entities.contacto.TipoTelefono;

/**
 * DTO plano de salida que proyecta cualquiera de las subclases de {@code Contacto},
 * conforme al schema {@code ContactoResponse} de {@code 04-openapi.yaml}.
 */
public record ContactoResponse(
        String id,
        String medio,
        TipoContacto tipoContacto,
        String observacion,
        String email,
        String telefono,
        TipoTelefono tipoTelefono
) implements Identificable {
}

