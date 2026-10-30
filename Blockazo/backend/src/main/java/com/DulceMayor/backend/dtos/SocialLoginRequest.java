package com.DulceMayor.backend.dtos;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de entrada para la autenticación o registro mediante token de Auth0 / Social Login.
 */
public record SocialLoginRequest(
        @NotBlank(message = "El token de identidad es obligatorio")
        String token
) {
}

