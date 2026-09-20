package com.example.zero.dto.compraProveedor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.zero.ecommerce.dto.contacto.ContactoTelefonicoDTO;
import com.zero.ecommerce.dto.contacto.ContactoCorreoElectronicoDTO;
import java.util.List;

/**
 * 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProveedorDTO {
    private String id;
    private String razonSocial;
    private List<com.zero.ecommerce.dto.contacto.ContactoTelefonicoDTO> contactosTelefonicos;
    private List<com.zero.ecommerce.dto.contacto.ContactoCorreoElectronicoDTO> contactosCorreo;
}
