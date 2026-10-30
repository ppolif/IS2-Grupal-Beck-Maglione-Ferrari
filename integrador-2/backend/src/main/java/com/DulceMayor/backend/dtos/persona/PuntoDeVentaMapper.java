package com.DulceMayor.backend.dtos.persona;

import com.DulceMayor.backend.dtos.BaseMapper;
import com.DulceMayor.backend.dtos.direccion.DireccionMapper;
import com.DulceMayor.backend.entities.persona.PuntoDeVenta;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper MapStruct para la entidad {@link PuntoDeVenta}.
 */
@Mapper(
        componentModel = "spring",
        uses = {DireccionMapper.class, MinoristaMapper.class, EmpleadoMapper.class},
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface PuntoDeVentaMapper extends BaseMapper<PuntoDeVenta, PuntoDeVentaRequest, PuntoDeVentaResponse> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    @Mapping(target = "minorista", ignore = true)
    @Mapping(target = "direccion", ignore = true)
    @Mapping(target = "preventor", ignore = true)
    PuntoDeVenta aEntidad(PuntoDeVentaRequest request);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    @Mapping(target = "minorista", ignore = true)
    @Mapping(target = "direccion", ignore = true)
    @Mapping(target = "preventor", ignore = true)
    void actualizarEntidad(PuntoDeVentaRequest request, @MappingTarget PuntoDeVenta entidad);

    @Override
    @Mapping(target = "minorista", source = "minorista")
    @Mapping(target = "direccion", source = "direccion")
    @Mapping(target = "preventor", source = "preventor")
    PuntoDeVentaResponse aResponse(PuntoDeVenta entidad);
}
