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
public class DepartamentoDTO {
    private String id;
    private String nombre;
    private String idProvincia;
    private String nombreProvincia;
}
