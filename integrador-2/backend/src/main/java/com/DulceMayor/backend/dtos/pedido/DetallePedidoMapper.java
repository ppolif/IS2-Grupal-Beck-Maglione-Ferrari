package com.DulceMayor.backend.dtos.pedido;

import com.DulceMayor.backend.entities.pedido.DetallePedido;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper de MapStruct para proyectar {@link DetallePedido} a {@link DetallePedidoResponse}.
 */
@Mapper(componentModel = "spring")
public interface DetallePedidoMapper {

    @Mapping(target = "productoId", source = "producto.id")
    @Mapping(target = "productoNombre", source = "producto.nombre")
    @Mapping(target = "presentacion", source = "producto.presentacion")
    DetallePedidoResponse aResponse(DetallePedido detalle);

    List<DetallePedidoResponse> aResponseList(List<DetallePedido> detalles);
}

