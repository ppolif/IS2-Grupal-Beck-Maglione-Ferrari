package com.DulceMayor.backend.dtos;

import com.DulceMayor.backend.entities.SubCategoria;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper de MapStruct para convertir entre la entidad {@link SubCategoria} y sus DTOs.
 */
@Mapper(componentModel = "spring")
public interface SubCategoriaMapper extends BaseMapper<SubCategoria, SubCategoriaRequest, SubCategoriaResponse> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    SubCategoria aEntidad(SubCategoriaRequest request);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    void actualizarEntidad(SubCategoriaRequest request, @MappingTarget SubCategoria entidad);

    @Override
    @Mapping(target = "categoriaId", source = "categoria.id")
    SubCategoriaResponse aResponse(SubCategoria entidad);
}

