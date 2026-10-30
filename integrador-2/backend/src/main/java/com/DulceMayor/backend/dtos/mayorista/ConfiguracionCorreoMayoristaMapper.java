package com.DulceMayor.backend.dtos.mayorista;

import com.DulceMayor.backend.dtos.BaseMapper;
import com.DulceMayor.backend.entities.mayorista.ConfiguracionCorreoMayorista;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper MapStruct para {@link ConfiguracionCorreoMayorista}.
 */
@Mapper(componentModel = "spring")
public interface ConfiguracionCorreoMayoristaMapper
        extends BaseMapper<ConfiguracionCorreoMayorista, ConfiguracionCorreoMayoristaRequest, ConfiguracionCorreoMayoristaResponse> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    @Mapping(target = "mayorista", ignore = true)
    ConfiguracionCorreoMayorista aEntidad(ConfiguracionCorreoMayoristaRequest request);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    @Mapping(target = "mayorista", ignore = true)
    void actualizarEntidad(ConfiguracionCorreoMayoristaRequest request, @MappingTarget ConfiguracionCorreoMayorista entidad);

    @Override
    ConfiguracionCorreoMayoristaResponse aResponse(ConfiguracionCorreoMayorista entidad);
}

