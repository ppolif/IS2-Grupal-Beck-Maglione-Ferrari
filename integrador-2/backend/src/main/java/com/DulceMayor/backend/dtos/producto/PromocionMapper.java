package com.DulceMayor.backend.dtos.producto;

import com.DulceMayor.backend.dtos.BaseMapper;
import com.DulceMayor.backend.entities.producto.Promocion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper de MapStruct para la entidad {@link Promocion}.
 */
@Mapper(componentModel = "spring")
public interface PromocionMapper extends BaseMapper<Promocion, PromocionRequest, PromocionResponse> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    @Mapping(target = "producto", ignore = true)
    Promocion aEntidad(PromocionRequest request);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    @Mapping(target = "producto", ignore = true)
    void actualizarEntidad(PromocionRequest request, @MappingTarget Promocion entidad);

    @Override
    @Mapping(target = "productoId", source = "producto.id")
    @Mapping(target = "productoNombre", source = "producto.nombre")
    PromocionResponse aResponse(Promocion entidad);
}

