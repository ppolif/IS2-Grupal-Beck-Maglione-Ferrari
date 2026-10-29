package com.DulceMayor.backend.dtos.facturacion;

import com.DulceMayor.backend.entities.facturacion.DetalleFactura;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper de MapStruct para proyectar {@link DetalleFactura} a {@link DetalleFacturaResponse}.
 */
@Mapper(componentModel = "spring")
public interface DetalleFacturaMapper {

    @Mapping(target = "productoId", source = "producto.id")
    @Mapping(target = "productoNombre", source = "producto.nombre")
    DetalleFacturaResponse aResponse(DetalleFactura detalle);

    List<DetalleFacturaResponse> aResponseList(List<DetalleFactura> detalles);
}

