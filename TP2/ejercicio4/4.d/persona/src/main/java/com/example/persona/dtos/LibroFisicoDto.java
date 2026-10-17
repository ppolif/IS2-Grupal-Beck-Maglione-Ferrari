package com.example.persona.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LibroFisicoDto extends LibroDto {
    private String numeroEstante;
    private Integer pesoGramos;
}

