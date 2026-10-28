package com.DulceMayor.backend.dtos;

import com.DulceMayor.backend.entities.Provincia;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper de MapStruct para convertir entre la entidad {@link Provincia} y sus DTOs.
 */
@Mapper(componentModel = "spring")
public interface ProvinciaMapper extends BaseMapper<Provincia, ProvinciaRequest, ProvinciaResponse> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    @Mapping(target = "pais", ignore = true)
    Provincia aEntidad(ProvinciaRequest request);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    @Mapping(target = "pais", ignore = true)
    void actualizarEntidad(ProvinciaRequest request, @MappingTarget Provincia entidad);

    @Override
    @Mapping(target = "paisId", source = "pais.id")
    ProvinciaResponse aResponse(Provincia entidad);
}

