package com.example.persona.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LibroDto extends BaseDto {
    private String titulo;
    private int fecha;
    private String genero;
    private int paginas;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaVencimientoDevolucion;

    private List<AutorDto> autores;
}
