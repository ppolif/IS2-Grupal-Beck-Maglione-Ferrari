package com.example.zero.dto.empresa;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.zero.ecommerce.dto.enums.TipoEmpresa;
import com.zero.ecommerce.dto.ubicacion.DireccionDTO;
import com.zero.ecommerce.dto.contacto.ContactoTelefonicoDTO;
import com.zero.ecommerce.dto.contacto.ContactoCorreoElectronicoDTO;
import java.util.List;

/**
 * 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaDTO {
    private String id;
    private String razonSocial;
    private String cuit;
    private TipoEmpresa tipoSucursal;
    private com.zero.ecommerce.dto.ubicacion.DireccionDTO direccion;
    private List<com.zero.ecommerce.dto.contacto.ContactoTelefonicoDTO> contactosTelefonicos;
    private List<com.zero.ecommerce.dto.contacto.ContactoCorreoElectronicoDTO> contactosCorreo;
}
