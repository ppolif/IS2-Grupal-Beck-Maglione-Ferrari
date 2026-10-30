package com.DulceMayor.backend.dtos.persona;

import com.DulceMayor.backend.dtos.ContactoRequest;
import com.DulceMayor.backend.dtos.direccion.DireccionRequest;
import com.DulceMayor.backend.entities.persona.TipoDocumento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO de entrada para registrar o actualizar un empleado según {@code 04-openapi.yaml}.
 */
public record EmpleadoRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 100, message = "El apellido no puede superar los 100 caracteres")
        String apellido,

        LocalDate fechaNacimiento,

        @NotNull(message = "El tipo de documento es obligatorio")
        TipoDocumento tipoDocumento,

        @NotBlank(message = "El número de documento es obligatorio")
        @Size(max = 30, message = "El número de documento no puede superar los 30 caracteres")
        String numeroDocumento,

        List<@Valid DireccionRequest> direcciones,

        List<@Valid ContactoRequest> contactos,

        @NotBlank(message = "El legajo es obligatorio")
        @Size(max = 50, message = "El legajo no puede superar los 50 caracteres")
        String legajo,

        @NotBlank(message = "El nombre de usuario es obligatorio")
        @Size(max = 100, message = "El nombre de usuario no puede superar los 100 caracteres")
        String nombreUsuario,

        @NotNull(message = "El rol es obligatorio")
        RolEmpleado rol
) {
}

