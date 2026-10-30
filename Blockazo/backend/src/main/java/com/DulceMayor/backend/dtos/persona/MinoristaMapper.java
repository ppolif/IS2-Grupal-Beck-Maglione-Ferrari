package com.DulceMayor.backend.dtos.persona;

import com.DulceMayor.backend.dtos.BaseMapper;
import com.DulceMayor.backend.dtos.direccion.DireccionMapper;
import com.DulceMayor.backend.entities.persona.Minorista;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

/**
 * Mapper MapStruct para la entidad {@link Minorista}.
 */
@Mapper(
        componentModel = "spring",
        uses = {DireccionMapper.class},
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface MinoristaMapper extends BaseMapper<Minorista, MinoristaRequest, MinoristaResponse> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "direcciones", ignore = true)
    Minorista aEntidad(MinoristaRequest request);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "direcciones", ignore = true)
    void actualizarEntidad(MinoristaRequest request, @MappingTarget Minorista entidad);

    @Override
    @Mapping(target = "tieneUsuario", source = "entidad", qualifiedByName = "evaluarTieneUsuario")
    MinoristaResponse aResponse(Minorista entidad);

    @Named("evaluarTieneUsuario")
    default boolean evaluarTieneUsuario(Minorista entidad) {
        return entidad != null && entidad.getUsuario() != null;
    }

    default MinoristaResumen aResumen(Minorista minorista) {
        if (minorista == null) {
            return null;
        }
        String email = minorista.getUsuario() != null ? minorista.getUsuario().getNombreUsuario() : null;
        return new MinoristaResumen(
                minorista.getId(),
                minorista.getNombre(),
                minorista.getApellido(),
                email
        );
    }
}
