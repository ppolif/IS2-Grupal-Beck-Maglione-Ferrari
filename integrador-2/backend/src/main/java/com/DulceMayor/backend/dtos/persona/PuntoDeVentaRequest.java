package com.DulceMayor.backend.dtos.persona;

import com.DulceMayor.backend.dtos.direccion.DireccionRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para registrar o modificar un punto de venta según {@code 04-openapi.yaml}.
 */
public record PuntoDeVentaRequest(
        @NotBlank(message = "El nombre de comercio es obligatorio")
        @Size(max = 150, message = "El nombre de comercio no puede superar los 150 caracteres")
        String nombreComercio,

        @NotBlank(message = "El id del minorista es obligatorio")
        String minoristaId,

        @NotNull(message = "La dirección es obligatoria")
        @Valid
        DireccionRequest direccion,

        String preventorId
) {
}

