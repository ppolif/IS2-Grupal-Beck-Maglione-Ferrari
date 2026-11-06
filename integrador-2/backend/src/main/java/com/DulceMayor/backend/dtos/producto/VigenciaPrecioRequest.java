package com.DulceMayor.backend.dtos.producto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO de entrada para registrar o actualizar el precio de un producto.
 * La fecha desde se fija automáticamente a la fecha actual y la fecha hasta se cierra
 * cuando se registra una nueva vigencia.
 */
public record VigenciaPrecioRequest(
        @NotNull(message = "El precio es obligatorio")
        @Positive(message = "El precio debe ser un número positivo mayor a cero")
        Double precio
) {
}

