package com.DulceMayor.backend.dtos;

/**
 * DTO de salida que representa una categoría activa del catálogo, conforme al schema
 * {@code CategoriaResponse} de {@code 04-openapi.yaml}.
 *
 * <p><b>Patrón aplicado: DTO (Data Transfer Object).</b></p>
 * <p><b>Por qué se aplica acá:</b> Expone hacia los clientes REST únicamente los atributos
 * públicos acordados en el contrato ({@code id} y {@code nombre}), ocultando detalles de
 * persistencia como el atributo {@code eliminado} de {@code EntidadBase}.</p>
 */
public record CategoriaResponse(
        String id,
        String nombre
) {
}

