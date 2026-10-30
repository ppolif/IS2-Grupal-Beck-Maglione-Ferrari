package com.DulceMayor.backend.dtos.mayorista;

import com.DulceMayor.backend.dtos.ContactoRequest;
import com.DulceMayor.backend.dtos.direccion.DireccionRequest;
import com.DulceMayor.backend.entities.mayorista.TipoEmpresa;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * DTO de entrada para registrar o modificar una sucursal o sede central mayorista,
 * según {@code 04-openapi.yaml}.
 */
public record MayoristaRequest(
        @NotBlank(message = "La razón social es obligatoria")
        @Size(max = 150, message = "La razón social no puede superar los 150 caracteres")
        String razonSocial,

        @NotBlank(message = "El CUIT es obligatorio")
        @Size(max = 20, message = "El CUIT no puede superar los 20 caracteres")
        String cuit,

        @NotNull(message = "El tipo de empresa es obligatorio")
        TipoEmpresa tipoEmpresa,

        List<@Valid DireccionRequest> direcciones,

        List<@Valid ContactoRequest> contactos,

        List<String> imagenIds
) {
}

