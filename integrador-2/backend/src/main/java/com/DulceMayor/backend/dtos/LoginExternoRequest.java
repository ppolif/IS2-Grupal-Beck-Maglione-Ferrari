package com.DulceMayor.backend.dtos;

import com.DulceMayor.backend.entities.ProveedorAuth;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de entrada para la autenticación externa (OAuth2 / Social Login con Auth0 / Google / Facebook).
 * Conforme a la especificación OpenAPI (04-openapi.yaml).
 */
public record LoginExternoRequest(
        @NotNull(message = "El proveedor de autenticación es obligatorio")
        ProveedorAuth proveedor,

        @NotBlank(message = "El token externo es obligatorio")
        String tokenExterno
) {
}
