package com.DulceMayor.backend.dtos.mayorista;

import com.DulceMayor.backend.dtos.BaseMapper;
import com.DulceMayor.backend.dtos.ContactoMapper;
import com.DulceMayor.backend.dtos.direccion.DireccionMapper;
import com.DulceMayor.backend.entities.Imagen;
import com.DulceMayor.backend.entities.mayorista.Mayorista;
import java.util.Collections;
import java.util.List;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

/**
 * Mapper MapStruct para la entidad {@link Mayorista}.
 */
@Mapper(
        componentModel = "spring",
        uses = {DireccionMapper.class, ContactoMapper.class},
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface MayoristaMapper extends BaseMapper<Mayorista, MayoristaRequest, MayoristaResponse> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    @Mapping(target = "direcciones", ignore = true)
    @Mapping(target = "contactos", ignore = true)
    @Mapping(target = "imagenes", ignore = true)
    Mayorista aEntidad(MayoristaRequest request);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    @Mapping(target = "direcciones", ignore = true)
    @Mapping(target = "contactos", ignore = true)
    @Mapping(target = "imagenes", ignore = true)
    void actualizarEntidad(MayoristaRequest request, @MappingTarget Mayorista entidad);

    @Override
    @Mapping(target = "imagenIds", source = "imagenes", qualifiedByName = "imagenesToIds")
    MayoristaResponse aResponse(Mayorista entidad);

    @Named("imagenesToIds")
    default List<String> imagenesToIds(List<Imagen> imagenes) {
        if (imagenes == null) {
            return Collections.emptyList();
        }
        return imagenes.stream().map(Imagen::getId).toList();
    }
}

