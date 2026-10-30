package com.DulceMayor.backend.dtos.direccion;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para una dirección física.
 */
public record DireccionRequest(
        @NotBlank(message = "La calle es obligatoria")
        @Size(max = 150, message = "La calle no puede superar los 150 caracteres")
        String calle,

        @NotBlank(message = "El número es obligatorio")
        @Size(max = 50, message = "El número no puede superar los 50 caracteres")
        @JsonAlias({"numeracion", "numero"})
        String numero,

        @Size(max = 50, message = "La manzana/piso no puede superar los 50 caracteres")
        String manzanaPiso,

        @Size(max = 50, message = "La casa/departamento no puede superar los 50 caracteres")
        String casaDepartamento,

        String localidadId
) {
    public String numeracion() {
        return numero;
    }

    public DireccionRequest(String calle, String numero, String localidadId) {
        this(calle, numero, null, null, localidadId);
    }
}

