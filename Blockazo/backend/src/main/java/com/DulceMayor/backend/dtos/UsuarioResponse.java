package com.DulceMayor.backend.dtos;

import com.DulceMayor.backend.entities.ProveedorAuth;

/**
 * DTO de salida con la información pública de un usuario (sin exponer su clave).
 */
public record UsuarioResponse(
        String id,
        String nombreUsuario,
        boolean claveTemporal,
        ProveedorAuth proveedorAuth
) {
}
