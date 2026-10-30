package com.DulceMayor.backend.dtos.persona;

/**
 * Evento de dominio emitido tras la baja lógica de un Minorista para coordinar
 * desacopladamente la baja de sus Puntos de Venta (patrón Observer).
 */
public record MinoristaEliminadoEvent(String minoristaId) {
}

