package com.example.persona.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistroMigradoDto {
    private int numeroFila;
    private String lineaOriginal;
    private String nombre;
    private String apellido;
    private Integer dni;
    private String calle;
    private Integer numero;
    private String estado; // INGRESADO, ACTUALIZADO, ERROR
    private String mensaje;
}
