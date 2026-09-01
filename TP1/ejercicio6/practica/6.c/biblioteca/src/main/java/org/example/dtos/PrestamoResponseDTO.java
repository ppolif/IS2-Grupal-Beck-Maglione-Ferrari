package org.example.dtos;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrestamoResponseDTO {

    private String id;
    private LocalDate fechaPrestamo;
    private LocalDate fechaDevolucion;
    private boolean alta;

    private LibroResponseDTO libro;     // Ya lo tenemos creado
    private UsuarioResponseDTO usuario; // Ya lo tenemos creado
}
