package org.example.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrestamoRequestDTO {

    @NotNull(message = "El ISBN del libro es obligatorio")
    private Long isbnLibro;

    @NotBlank(message = "El ID del usuario es obligatorio")
    private String idUsuario;
}
