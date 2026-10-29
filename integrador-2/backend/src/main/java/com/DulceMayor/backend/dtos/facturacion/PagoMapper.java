package com.DulceMayor.backend.dtos.facturacion;

import com.DulceMayor.backend.dtos.BaseMapper;
import com.DulceMayor.backend.entities.facturacion.Pago;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper de MapStruct para convertir entre la entidad {@link Pago} y sus DTOs.
 */
@Mapper(componentModel = "spring")
public interface PagoMapper extends BaseMapper<Pago, PagoRequest, PagoResponse> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    @Mapping(target = "fechaPago", ignore = true)
    @Mapping(target = "formaDePago", ignore = true)
    @Mapping(target = "factura", ignore = true)
    Pago aEntidad(PagoRequest request);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    @Mapping(target = "fechaPago", ignore = true)
    @Mapping(target = "formaDePago", ignore = true)
    @Mapping(target = "factura", ignore = true)
    void actualizarEntidad(PagoRequest request, @MappingTarget Pago entidad);

    @Override
    @Mapping(target = "facturaId", source = "factura.id")
    PagoResponse aResponse(Pago entidad);
}
