package com.DulceMayor.backend.dtos.direccion;

import com.DulceMayor.backend.dtos.BaseMapper;
import com.DulceMayor.backend.entities.direccion.Pais;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper de MapStruct para convertir entre la entidad {@link Pais} y sus DTOs.
 */
@Mapper(componentModel = "spring")
public interface PaisMapper extends BaseMapper<Pais, PaisRequest, PaisResponse> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    Pais aEntidad(PaisRequest request);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    void actualizarEntidad(PaisRequest request, @MappingTarget Pais entidad);
}

