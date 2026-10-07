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
public class PersonaDto extends BaseDto {
    private String nombre;
    private String apellido;
    private int dni;
    private DomicilioDto domicilio;
    private Boolean tienePrestamo;
    private List<LibroDto> libros;
}
