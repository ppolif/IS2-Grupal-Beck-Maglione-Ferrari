package com.DulceMayor.backend.dtos.direccion;

import com.DulceMayor.backend.dtos.BaseMapper;
import com.DulceMayor.backend.entities.direccion.Localidad;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper de MapStruct para convertir entre la entidad {@link Localidad} y sus DTOs.
 */
@Mapper(componentModel = "spring")
public interface LocalidadMapper extends BaseMapper<Localidad, LocalidadRequest, LocalidadResponse> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    @Mapping(target = "departamento", ignore = true)
    Localidad aEntidad(LocalidadRequest request);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    @Mapping(target = "departamento", ignore = true)
    void actualizarEntidad(LocalidadRequest request, @MappingTarget Localidad entidad);

    @Override
    @Mapping(target = "departamentoId", source = "departamento.id")
    LocalidadResponse aResponse(Localidad entidad);
}

