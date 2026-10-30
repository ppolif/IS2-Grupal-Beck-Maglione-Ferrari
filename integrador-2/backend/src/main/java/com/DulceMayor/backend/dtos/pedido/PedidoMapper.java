package com.DulceMayor.backend.dtos.pedido;

import com.DulceMayor.backend.dtos.BaseMapper;
import com.DulceMayor.backend.dtos.persona.EmpleadoMapper;
import com.DulceMayor.backend.dtos.persona.PuntoDeVentaMapper;
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
        uses = {DetallePedidoMapper.class, PuntoDeVentaMapper.class, EmpleadoMapper.class},
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
    @Mapping(target = "puntoDeVenta", ignore = true)
    @Mapping(target = "preventor", ignore = true)
    @Mapping(target = "distribuidor", ignore = true)
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
    @Mapping(target = "puntoDeVenta", ignore = true)
    @Mapping(target = "preventor", ignore = true)
    @Mapping(target = "distribuidor", ignore = true)
    @Mapping(target = "promocion", ignore = true)
    @Mapping(target = "detalles", ignore = true)
    void actualizarEntidad(PedidoRequest request, @MappingTarget Pedido entidad);

    @Override
    @Mapping(target = "codigoPromocion", source = "promocion.codigo")
    @Mapping(target = "facturaId", ignore = true)
    @Mapping(target = "puntoDeVenta", source = "puntoDeVenta")
    @Mapping(target = "preventor", source = "preventor")
    @Mapping(target = "distribuidor", source = "distribuidor")
    PedidoResponse aResponse(Pedido entidad);
}
