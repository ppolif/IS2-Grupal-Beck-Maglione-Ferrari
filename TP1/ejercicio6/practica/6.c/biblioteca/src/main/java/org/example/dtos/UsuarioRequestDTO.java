package org.example.dtos;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioRequestDTO {

    @NotNull(message = "El DNI es obligatorio")
    private Long dni;

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    private String telefono;

    @NotBlank(message = "El mail es obligatorio")
    @Email(message = "El formato del mail es inválido")
    private String mail;

    @NotBlank(message = "La clave es obligatoria")
    @Size(min = 6, message = "La clave debe tener al menos 6 caracteres")
    private String clave;
}
