package com.DulceMayor.backend.dtos;

import com.DulceMayor.backend.entities.ProveedorAuth;
import com.DulceMayor.backend.entities.RolUsuario;

/**
 * DTO de salida con la información pública de un usuario (sin exponer su clave).
 */
public record UsuarioResponse(
        String id,
        String nombreUsuario,
        RolUsuario rol,
        boolean claveTemporal,
        ProveedorAuth proveedorAuth
) {
}

