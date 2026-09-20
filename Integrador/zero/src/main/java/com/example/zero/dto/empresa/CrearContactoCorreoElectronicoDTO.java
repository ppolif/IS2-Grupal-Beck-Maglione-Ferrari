package com.example.zero.dto.empresa;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.zero.ecommerce.dto.enums.TipoContacto;

/**
 * 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearContactoCorreoElectronicoDTO {
    private String email;
    private TipoContacto tipoContacto;
    private String observacion;
}
