package com.example.zero.dto.compraProveedor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.zero.ecommerce.dto.contacto.CrearContactoTelefonicoDTO;
import com.zero.ecommerce.dto.contacto.CrearContactoCorreoElectronicoDTO;
import java.util.List;

/**
 * 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearProveedorDTO {
    private String razonSocial;
    private List<com.zero.ecommerce.dto.contacto.CrearContactoTelefonicoDTO> contactosTelefonicos;
    private List<com.zero.ecommerce.dto.contacto.CrearContactoCorreoElectronicoDTO> contactosCorreo;
}
