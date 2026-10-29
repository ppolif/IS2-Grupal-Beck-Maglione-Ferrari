package com.DulceMayor.backend.dtos.facturacion;

import com.DulceMayor.backend.dtos.BaseMapper;
import com.DulceMayor.backend.entities.facturacion.FormaDePago;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper de MapStruct para convertir entre la entidad {@link FormaDePago} y sus DTOs.
 */
@Mapper(componentModel = "spring")
public interface FormaDePagoMapper extends BaseMapper<FormaDePago, FormaDePagoRequest, FormaDePagoResponse> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    FormaDePago aEntidad(FormaDePagoRequest request);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    void actualizarEntidad(FormaDePagoRequest request, @MappingTarget FormaDePago entidad);
}

