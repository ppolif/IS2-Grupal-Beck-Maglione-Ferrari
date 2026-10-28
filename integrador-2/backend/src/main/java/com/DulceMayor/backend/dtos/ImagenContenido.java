package com.DulceMayor.backend.dtos;

/**
 * Registro de transferencia interna con el contenido binario y tipo MIME para streaming o descarga.
 */
public record ImagenContenido(
        byte[] contenido,
        String mime
) {
}

