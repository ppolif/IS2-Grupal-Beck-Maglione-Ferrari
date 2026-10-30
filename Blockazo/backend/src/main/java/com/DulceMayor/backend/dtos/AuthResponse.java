package com.DulceMayor.backend.dtos;

/**
 * DTO de salida para la respuesta de autenticación exitosa.
 */
public record AuthResponse(
        String token,
        String tipo,
        long expiraEn,
        boolean debeCambiarClave,
        UsuarioResponse usuario
) {
}

