package com.DulceMayor.backend.dtos.persona;

import com.DulceMayor.backend.entities.persona.TipoDocumento;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * DTO de entrada para el auto-registro público de un cliente minorista en el portal
 * vía {@code POST /auth/registro}.
 */
public record RegistroMinoristaRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 100, message = "El apellido no puede superar los 100 caracteres")
        String apellido,

        @NotNull(message = "El tipo de documento es obligatorio")
        TipoDocumento tipoDocumento,

        @NotBlank(message = "El número de documento es obligatorio")
        @Size(max = 30, message = "El número de documento no puede superar los 30 caracteres")
        String numeroDocumento,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato del correo electrónico no es válido")
        @Size(max = 150, message = "El correo no puede superar los 150 caracteres")
        String email,

        @NotBlank(message = "La clave es obligatoria")
        @Size(min = 8, message = "La clave debe tener al menos 8 caracteres")
        String clave,

        @Size(max = 150, message = "El nombre de comercio no puede superar los 150 caracteres")
        String nombreComercio,

        @Past(message = "La fecha de nacimiento debe ser anterior a la actual")
        LocalDate fechaNacimiento,

        @Size(max = 30, message = "El teléfono no puede superar los 30 caracteres")
        String telefono
) {
    public RegistroMinoristaRequest(
            String nombre,
            String apellido,
            TipoDocumento tipoDocumento,
            String numeroDocumento,
            String email,
            String clave,
            String nombreComercio) {
        this(nombre, apellido, tipoDocumento, numeroDocumento, email, clave, nombreComercio, null, null);
    }
}
