package com.example.zero.dto.empresa;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.zero.ecommerce.dto.enums.TipoEmpresa;
import java.util.List;

/**
 * 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearEmpresaDTO {
    private String razonSocial;
    private String cuit;
    private TipoEmpresa tipoSucursal;
    private String idDireccion;
    private List<String> idsContactoTelefonico;
    private List<String> idsContactoCorreo;
}
