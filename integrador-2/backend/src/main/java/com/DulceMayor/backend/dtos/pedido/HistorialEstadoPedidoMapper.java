package com.DulceMayor.backend.dtos.pedido;

import com.DulceMayor.backend.entities.pedido.HistorialEstadoPedido;
import java.util.List;
import org.mapstruct.Mapper;

/**
 * Mapper de MapStruct para proyectar {@link HistorialEstadoPedido} a {@link HistorialEstadoPedidoResponse}.
 */
@Mapper(componentModel = "spring")
public interface HistorialEstadoPedidoMapper {

    HistorialEstadoPedidoResponse aResponse(HistorialEstadoPedido historial);

    List<HistorialEstadoPedidoResponse> aResponseList(List<HistorialEstadoPedido> historiales);
}

