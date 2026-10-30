package com.DulceMayor.backend.dtos.facturacion;

import com.DulceMayor.backend.dtos.BaseMapper;
import com.DulceMayor.backend.entities.facturacion.Factura;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper de MapStruct para convertir entre la entidad {@link Factura} y sus DTOs.
 */
@Mapper(
        componentModel = "spring",
        uses = {DetalleFacturaMapper.class},
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface FacturaMapper extends BaseMapper<Factura, FacturaRequest, FacturaResponse> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    @Mapping(target = "numeroFactura", ignore = true)
    @Mapping(target = "fechaFactura", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "pedido", ignore = true)
    @Mapping(target = "detalles", ignore = true)
    Factura aEntidad(FacturaRequest request);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    @Mapping(target = "numeroFactura", ignore = true)
    @Mapping(target = "fechaFactura", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "pedido", ignore = true)
    @Mapping(target = "detalles", ignore = true)
    void actualizarEntidad(FacturaRequest request, @MappingTarget Factura entidad);

    @Override
    @Mapping(target = "pedidoId", source = "pedido.id")
    @Mapping(target = "totalPagado", ignore = true)
    @Mapping(target = "saldo", ignore = true)
    @Mapping(target = "pagos", ignore = true)
    FacturaResponse aResponse(Factura entidad);
}
