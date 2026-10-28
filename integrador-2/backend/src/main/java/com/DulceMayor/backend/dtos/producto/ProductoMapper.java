package com.DulceMayor.backend.dtos.producto;

import com.DulceMayor.backend.dtos.BaseMapper;
import com.DulceMayor.backend.entities.producto.Producto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper de MapStruct para la entidad {@link Producto}.
 */
@Mapper(componentModel = "spring")
public interface ProductoMapper extends BaseMapper<Producto, ProductoRequest, ProductoResponse> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    @Mapping(target = "subCategoria", ignore = true)
    @Mapping(target = "imagen", ignore = true)
    @Mapping(target = "enOferta", defaultValue = "false")
    Producto aEntidad(ProductoRequest request);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    @Mapping(target = "subCategoria", ignore = true)
    @Mapping(target = "imagen", ignore = true)
    void actualizarEntidad(ProductoRequest request, @MappingTarget Producto entidad);

    @Override
    @Mapping(target = "imagenId", source = "imagen.id")
    @Mapping(target = "subCategoriaId", source = "subCategoria.id")
    @Mapping(target = "subCategoriaNombre", source = "subCategoria.nombre")
    @Mapping(target = "categoriaId", source = "subCategoria.categoria.id")
    @Mapping(target = "categoriaNombre", source = "subCategoria.categoria.nombre")

    ///el precio y stock se ignoran porque no estan en la tabla de producto
    @Mapping(target = "precioVigente", ignore = true)
    @Mapping(target = "stockActual", ignore = true)
    ProductoResponse aResponse(Producto entidad);
}

