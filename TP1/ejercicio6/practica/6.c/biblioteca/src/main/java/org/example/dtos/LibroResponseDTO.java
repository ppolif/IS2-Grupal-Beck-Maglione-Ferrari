package org.example.dtos;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LibroResponseDTO {

    private Long isbn;
    private String titulo;
    private int anio;
    private int ejemplares;
    private int ejemplaresPrestados;
    private int ejemplaresRestantes;
    private boolean alta;

    // Objetos anidados para entregar la información completa
    private EditorialResponseDTO editorial;
    private List<AutorResponseDTO> autores;
    private ImagenResponseDTO imagen;
}
