package com.example.persona.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LibroDigitalDto extends LibroDto {
    private Double tamanoMB;
    private String formato;
    private String urlDescarga;
}

