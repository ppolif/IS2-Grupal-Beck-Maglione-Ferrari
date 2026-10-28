package com.DulceMayor.backend.dtos;

import com.DulceMayor.backend.entities.Departamento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper de MapStruct para convertir entre la entidad {@link Departamento} y sus DTOs.
 */
@Mapper(componentModel = "spring")
public interface DepartamentoMapper extends BaseMapper<Departamento, DepartamentoRequest, DepartamentoResponse> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    @Mapping(target = "provincia", ignore = true)
    Departamento aEntidad(DepartamentoRequest request);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    @Mapping(target = "provincia", ignore = true)
    void actualizarEntidad(DepartamentoRequest request, @MappingTarget Departamento entidad);

    @Override
    @Mapping(target = "provinciaId", source = "provincia.id")
    DepartamentoResponse aResponse(Departamento entidad);
}

