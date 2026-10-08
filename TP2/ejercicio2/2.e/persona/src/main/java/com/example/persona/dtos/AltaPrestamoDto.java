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
public class AltaPrestamoDto {
    private Long personaId;
    private Long libroId;
    private LocalDate fechaPrestamo;
}
