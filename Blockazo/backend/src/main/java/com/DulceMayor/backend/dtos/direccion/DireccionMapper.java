package com.DulceMayor.backend.dtos.direccion;

import com.DulceMayor.backend.dtos.BaseMapper;
import com.DulceMayor.backend.entities.direccion.Direccion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper de MapStruct para convertir entre la entidad {@link Direccion} y sus DTOs.
 */
@Mapper(componentModel = "spring")
public interface DireccionMapper extends BaseMapper<Direccion, DireccionRequest, DireccionResponse> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    @Mapping(target = "localidad", ignore = true)
    Direccion aEntidad(DireccionRequest request);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    @Mapping(target = "localidad", ignore = true)
    void actualizarEntidad(DireccionRequest request, @MappingTarget Direccion entidad);

    @Override
    @Mapping(target = "localidadId", source = "localidad.id")
    @Mapping(target = "localidadNombre", source = "localidad.nombre")
    @Mapping(target = "departamentoId", source = "localidad.departamento.id")
    @Mapping(target = "departamentoNombre", source = "localidad.departamento.nombre")
    @Mapping(target = "provinciaNombre", source = "localidad.departamento.provincia.nombre")
    DireccionResponse aResponse(Direccion entidad);
}

