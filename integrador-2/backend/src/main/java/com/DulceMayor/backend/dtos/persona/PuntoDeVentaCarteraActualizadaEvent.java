package com.DulceMayor.backend.dtos.persona;

/**
 * Evento de dominio emitido cuando se da de alta o modifica un punto de venta
 * asignado a la cartera de un preventor, permitiendo recalcular automáticamente
 * con OSRM su hoja de ruta activa del día.
 * esto es para que si se agregan puntos de venta a la cartera de un preventor, se pueda recalcular la ruta del día y optimizarla.
 */
public record PuntoDeVentaCarteraActualizadaEvent(
        String preventorId,
        String puntoDeVentaId
) {
}
