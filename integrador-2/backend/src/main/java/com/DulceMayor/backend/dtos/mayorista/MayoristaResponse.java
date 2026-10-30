package com.DulceMayor.backend.dtos.mayorista;

import com.DulceMayor.backend.dtos.ContactoResponse;
import com.DulceMayor.backend.dtos.Identificable;
import com.DulceMayor.backend.dtos.direccion.DireccionResponse;
import com.DulceMayor.backend.entities.mayorista.TipoEmpresa;
import java.util.List;

/**
 * DTO de salida para representar una sucursal o sede central mayorista,
 * según {@code 04-openapi.yaml}.
 */
public record MayoristaResponse(
        String id,
        String razonSocial,
        String cuit,
        TipoEmpresa tipoEmpresa,
        List<DireccionResponse> direcciones,
        List<ContactoResponse> contactos,
        List<String> imagenIds
) implements Identificable {
}

