package com.DulceMayor.backend.dtos.direccion;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para una dirección física, conforme al schema
 * {@code DireccionRequest} de {@code 04-openapi.yaml}.
 */
public record DireccionRequest(
        @NotBlank(message = "La calle es obligatoria")
        @Size(max = 150, message = "La calle no puede superar los 150 caracteres")
        String calle,

        @NotBlank(message = "La numeración es obligatoria")
        @Size(max = 50, message = "La numeración no puede superar los 50 caracteres")
        String numeracion,

        @Size(max = 100, message = "El barrio no puede superar los 100 caracteres")
        String barrio,

        @Size(max = 50, message = "La manzana/piso no puede superar los 50 caracteres")
        String manzanaPiso,

        @Size(max = 50, message = "La casa/departamento no puede superar los 50 caracteres")
        String casaDepartamento,

        @Size(max = 255, message = "La referencia no puede superar los 255 caracteres")
        String referencia,

        @DecimalMin(value = "-90.0", message = "La latitud debe ser mayor o igual a -90")
        @DecimalMax(value = "90.0", message = "La latitud debe ser menor o igual a 90")
        Double latitud,

        @DecimalMin(value = "-180.0", message = "La longitud debe ser mayor o igual a -180")
        @DecimalMax(value = "180.0", message = "La longitud debe ser menor o igual a 180")
        Double longitud,

        @NotBlank(message = "El id de la localidad es obligatorio")
        String localidadId
) {
}

