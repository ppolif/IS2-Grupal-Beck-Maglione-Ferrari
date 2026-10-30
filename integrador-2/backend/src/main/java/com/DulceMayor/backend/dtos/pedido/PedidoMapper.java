package com.DulceMayor.backend.dtos.pedido;

import com.DulceMayor.backend.dtos.BaseMapper;
import com.DulceMayor.backend.entities.pedido.Pedido;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper de MapStruct para convertir entre la entidad {@link Pedido} y sus DTOs.
 */
@Mapper(
        componentModel = "spring",
        uses = {DetallePedidoMapper.class},
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface PedidoMapper extends BaseMapper<Pedido, PedidoRequest, PedidoResponse> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    @Mapping(target = "numeroPedido", ignore = true)
    @Mapping(target = "fechaHoraCarga", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "descuento", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "distribuidorId", ignore = true)
    @Mapping(target = "promocion", ignore = true)
    @Mapping(target = "detalles", ignore = true)
    Pedido aEntidad(PedidoRequest request);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    @Mapping(target = "numeroPedido", ignore = true)
    @Mapping(target = "fechaHoraCarga", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "descuento", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "distribuidorId", ignore = true)
    @Mapping(target = "promocion", ignore = true)
    @Mapping(target = "detalles", ignore = true)
    void actualizarEntidad(PedidoRequest request, @MappingTarget Pedido entidad);

    @Override
    @Mapping(target = "codigoPromocion", source = "promocion.codigo")
    @Mapping(target = "facturaId", ignore = true)
    PedidoResponse aResponse(Pedido entidad);
}

