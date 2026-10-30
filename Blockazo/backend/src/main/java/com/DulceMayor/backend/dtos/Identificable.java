package com.DulceMayor.backend.dtos;

/**
 * Interfaz común para DTOs que exponen un identificador único (ID).
 * Permite que componentes genéricos (como {@code BaseController}) puedan
 * acceder al ID de forma fuertemente tipada sin obligar a subclases a redefinir extractores.
 */
public interface Identificable {

    /**
     * Devuelve el identificador único del recurso.
     */
    String id();
}

