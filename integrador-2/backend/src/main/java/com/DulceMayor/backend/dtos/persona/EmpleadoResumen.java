package com.DulceMayor.backend.dtos.persona;

import com.DulceMayor.backend.entities.RolUsuario;

/**
 * Resumen de un empleado utilizado en respuestas anidadas de pedidos y puntos de venta.
 */
public record EmpleadoResumen(
        String id,
        String nombre,
        String apellido,
        RolUsuario rol
) {
}

