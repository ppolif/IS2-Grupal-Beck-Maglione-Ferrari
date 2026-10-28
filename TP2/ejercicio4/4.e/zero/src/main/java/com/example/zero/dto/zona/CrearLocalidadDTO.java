package com.example.zero.dto.zona;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearLocalidadDTO {
    private String nombre;
    private String codigoPostal;
    private String idDepartamento;
}
