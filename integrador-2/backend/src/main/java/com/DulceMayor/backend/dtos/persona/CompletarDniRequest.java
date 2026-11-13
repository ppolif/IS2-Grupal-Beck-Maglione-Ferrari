package com.DulceMayor.backend.dtos.persona;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para registrar por única vez el DNI real de un cliente minorista
 * autenticado mediante proveedor externo (Auth0 / Google).
 */
public record CompletarDniRequest(
        @NotBlank(message = "El número de DNI es obligatorio")
        @Size(max = 30, message = "El número de DNI no puede superar los 30 caracteres")
        String numeroDocumento,

        String confirmarNumeroDocumento
) {
}
