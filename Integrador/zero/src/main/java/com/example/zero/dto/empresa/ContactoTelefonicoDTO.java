package com.example.zero.dto.empresa;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.zero.ecommerce.dto.enums.TipoTelefono;
import com.zero.ecommerce.dto.enums.TipoContacto;

/**
 * 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactoTelefonicoDTO {
    private String id;
    private String telefono;
    private TipoTelefono tipoTelefono;
    private TipoContacto tipoContacto;
    private String observacion;
}
