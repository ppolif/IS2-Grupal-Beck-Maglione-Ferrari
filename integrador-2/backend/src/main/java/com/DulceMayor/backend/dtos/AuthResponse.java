package com.DulceMayor.backend.dtos;

/**
 * DTO de salida para la respuesta de autenticación exitosa.
 */
public record AuthResponse(
        /// el frontend guarda este token (en localStorage o memoria) y lo envía en la cabecera Authorization en
        /// cada petición posterior para identificarse
        String token,

        /// Le indica al cliente qué esquema de autenticación HTTP debe utilizar para adjuntar el token. Gracias a esto,
        /// el frontend sabe que debe armar el header exactamente así: Authorization: Bearer <token>
        String tipo,
        long expiraEn,
        boolean debeCambiarClave,
        UsuarioResponse usuario
) {
}

