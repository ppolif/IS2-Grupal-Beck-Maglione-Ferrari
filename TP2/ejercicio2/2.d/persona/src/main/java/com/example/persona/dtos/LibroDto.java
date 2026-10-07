package com.example.persona.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private Boolean prestado;
}
