package com.DulceMayor.backend.dtos;

import com.DulceMayor.backend.entities.Categoria;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper de MapStruct para convertir entre la entidad {@link Categoria} y sus DTOs
 * ({@link CategoriaRequest} y {@link CategoriaResponse}).
 *
 * <p><b>Patrón aplicado: DTO (Data Transfer Object) + Mapper.</b></p>
 * <p><b>Por qué se aplica acá:</b> Automatiza en tiempo de compilación la traducción entre la
 * capa de dominio y los objetos de transferencia de datos sin reflexión en tiempo de ejecución,
 * garantizando que {@code id} y {@code eliminado} no sean sobreescritos desde el DTO de entrada.</p>
 */
@Mapper(componentModel = "spring")
public interface CategoriaMapper extends BaseMapper<Categoria, CategoriaRequest, CategoriaResponse> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    Categoria aEntidad(CategoriaRequest request);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    void actualizarEntidad(CategoriaRequest request, @MappingTarget Categoria entidad);
}

