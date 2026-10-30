package com.DulceMayor.backend.dtos.mayorista;

import com.DulceMayor.backend.dtos.Identificable;

/**
 * DTO de salida para la configuración de correo SMTP de un mayorista,
 * según {@code 04-openapi.yaml}. Omite siempre la clave por motivos de seguridad.
 */
public record ConfiguracionCorreoMayoristaResponse(
        String id,
        String correo,
        String puerto,
        String smtp,
        boolean tls
) implements Identificable {
}

