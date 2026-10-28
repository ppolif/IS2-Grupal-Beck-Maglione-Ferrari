package com.example.persona.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MigracionResultadoDto {
    private String nombreArchivo;
    private int totalLeidos;
    private int totalExitosos;
    private int totalFallidos;
    private List<RegistroMigradoDto> registros = new ArrayList<>();
    private String mensaje;
}
