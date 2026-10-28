package com.DulceMayor.backend.dtos;

import com.DulceMayor.backend.entities.contacto.TipoContacto;
import com.DulceMayor.backend.entities.contacto.TipoTelefono;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO plano de entrada para crear o actualizar un contacto polimórfico, conforme al schema
 * {@code ContactoRequest} de {@code 04-openapi.yaml}.
 *
 * <ul>
 *   <li>Si {@code medio = "CORREO"}, el campo {@code email} es obligatorio.</li>
 *   <li>Si {@code medio = "TELEFONICO"}, los campos {@code telefono} y {@code tipoTelefono} son obligatorios.</li>
 * </ul>
 */
public record ContactoRequest(
        @NotBlank(message = "El medio de contacto es obligatorio")
        @Pattern(
                regexp = "^(CORREO|TELEFONICO)$",
                message = "El medio de contacto debe ser CORREO o TELEFONICO"
        )
        String medio,

        @NotNull(message = "El tipo de contacto es obligatorio")
        TipoContacto tipoContacto,

        @Size(max = 255, message = "La observación no puede superar los 255 caracteres")
        String observacion,

        @Email(message = "El formato del correo electrónico no es válido")
        @Size(max = 150, message = "El correo electrónico no puede superar los 150 caracteres")
        String email,

        @Size(max = 30, message = "El teléfono no puede superar los 30 caracteres")
        String telefono,

        TipoTelefono tipoTelefono
) {
}

