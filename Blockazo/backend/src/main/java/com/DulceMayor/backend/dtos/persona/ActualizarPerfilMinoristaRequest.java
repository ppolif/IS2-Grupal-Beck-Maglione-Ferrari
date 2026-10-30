package com.DulceMayor.backend.dtos.persona;

import com.DulceMayor.backend.entities.persona.TipoDocumento;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * DTO de entrada para la actualización del perfil del cliente minorista autenticado
 * vía {@code PUT /api/v1/mi-cuenta/perfil}.
 */
public record ActualizarPerfilMinoristaRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 100, message = "El apellido no puede superar los 100 caracteres")
        String apellido,

        @Past(message = "La fecha de nacimiento debe ser anterior a la actual")
        LocalDate fechaNacimiento,

        @NotNull(message = "El tipo de documento es obligatorio")
        TipoDocumento tipoDocumento,

        @NotBlank(message = "El número de documento es obligatorio")
        @Size(max = 30, message = "El número de documento no puede superar los 30 caracteres")
        String numeroDocumento,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato del correo electrónico no es válido")
        @Size(max = 150, message = "El correo no puede superar los 150 caracteres")
        String email,


        @Size(max = 150, message = "La calle no puede superar los 150 caracteres")
        String calle,

        @Size(max = 50, message = "El número no puede superar los 50 caracteres")
        String numero
) {
    public ActualizarPerfilMinoristaRequest(
            String nombre,
            String apellido,
            LocalDate fechaNacimiento,
            TipoDocumento tipoDocumento,
            String numeroDocumento,
            String email,
            String telefono) {
        this(nombre, apellido, fechaNacimiento, tipoDocumento, numeroDocumento, email, telefono, null, null);
    }
}

