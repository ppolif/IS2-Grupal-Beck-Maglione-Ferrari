package com.DulceMayor.backend.dtos;

/**
 * DTO de salida para los datos de perfil del usuario autenticado.
 */
public record PerfilResponse(
        UsuarioResponse usuario,
        String personaId,
        String nombre,
        String apellido
) {
}

