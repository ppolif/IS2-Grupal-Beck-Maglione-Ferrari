package com.DulceMayor.backend.dtos.ruta;

/**
 * DTO de respuesta devuelto al registrar una parada o entrega completada,
 * informando el estado actualizado de la ruta y la próxima parada recomendada en el mapa.
 */
public record AvanceRutaResponse(
        RutaResponse ruta,
        ParadaResponse siguienteParada,
        boolean rutaFinalizada
) {
}

