package com.DulceMayor.backend.entities.ruta;

/**
 * Enumeración que representa el estado en el que se encuentra una ruta diaria.
 *
 * Conforme a {@code 01-dominio.md} y {@code 04-openapi.yaml}:
 * <ul>
 *   <li>{@link #PENDIENTE}: La ruta fue generada/armada y se encuentra en curso de atención o esperando inicio.</li>
 *   <li>{@link #REALIZADA}: Todas las paradas planificadas para el día fueron visitadas o entregadas exitosamente.</li>
 *   <li>{@link #INCOMPLETA}: La jornada finalizó o la ruta fue cerrada manualmente antes de completar la totalidad de las paradas.</li>
 * </ul>
 */
public enum RutaEstado {
    PENDIENTE,
    REALIZADA,
    INCOMPLETA
}

