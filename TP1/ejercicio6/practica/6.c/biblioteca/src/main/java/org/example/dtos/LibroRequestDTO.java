package org.example.dtos;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LibroRequestDTO {

    @NotNull(message = "El ISBN es obligatorio")
    private Long isbn;

    @NotBlank(message = "El título no puede estar vacío")
    private String titulo;

    @NotNull(message = "El año es obligatorio")
    private Integer anio;

    @NotNull(message = "La cantidad de ejemplares es obligatoria")
    @Min(value = 1, message = "Debe haber al menos 1 ejemplar")
    private Integer ejemplares;

    @NotEmpty(message = "El libro debe tener al menos un autor")
    private List<String> idAutores;

    @NotBlank(message = "El libro debe tener una editorial")
    private String idEditorial;

    // La imagen es opcional (puede cargarse después)
    private String idImagen;
}