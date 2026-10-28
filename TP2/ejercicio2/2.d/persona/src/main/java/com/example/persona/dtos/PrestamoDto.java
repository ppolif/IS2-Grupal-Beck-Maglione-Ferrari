package com.example.persona.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrestamoDto extends BaseDto {
    private LocalDate fechaPrestamo;
    private LocalDate fechaDevolucion;
    private String estado;
    private PersonaDto persona;
    private LibroDto libro;
}
