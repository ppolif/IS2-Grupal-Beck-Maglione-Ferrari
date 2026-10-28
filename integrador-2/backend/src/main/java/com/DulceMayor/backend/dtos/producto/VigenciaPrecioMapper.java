package com.DulceMayor.backend.dtos.producto;

import com.DulceMayor.backend.dtos.BaseMapper;
import com.DulceMayor.backend.entities.producto.VigenciaPrecio;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper de MapStruct para la entidad {@link VigenciaPrecio}.
 */
@Mapper(componentModel = "spring")
public interface VigenciaPrecioMapper extends BaseMapper<VigenciaPrecio, VigenciaPrecioRequest, VigenciaPrecioResponse> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    @Mapping(target = "producto", ignore = true)
    VigenciaPrecio aEntidad(VigenciaPrecioRequest request);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    @Mapping(target = "producto", ignore = true)
    void actualizarEntidad(VigenciaPrecioRequest request, @MappingTarget VigenciaPrecio entidad);

    @Override
    @Mapping(target = "productoId", source = "producto.id")
    VigenciaPrecioResponse aResponse(VigenciaPrecio entidad);
}

