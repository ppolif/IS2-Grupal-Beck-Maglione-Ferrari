package com.DulceMayor.backend.dtos.producto;

import com.DulceMayor.backend.entities.producto.Stock;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper para convertir entre la entidad {@link Stock} y su proyección {@link StockResponse}.
 *
 * <p><b>Patrón GoF aplicado: Adapter (Estructural) / Objeto Adaptador</b></p>
 * <ul>
 *   <li><b>Participante GoF:</b> Adapter (implementado por MapStruct en tiempo de compilación).</li>
 *   <li><b>Target:</b> {@link StockResponse} (la interfaz/contrato requerida por la capa web/OpenAPI).</li>
 *   <li><b>Adaptee:</b> {@link Stock} (la entidad interna del modelo de persistencia JPA).</li>
 *   <li><b>Rol:</b> Adapta la entidad de persistencia al formato del DTO esperado por el cliente HTTP.
 *       En el modelo UML final, {@code productoId} y {@code productoNombre} se navegarán a través de
 *       {@code DetalleFactura}. Mientras dicha entidad no exista, se ignoran en el mapeo.</li>
 * </ul>
 */
@Mapper(componentModel = "spring")
public interface StockMapper {

    /*
     * Mapeo hacia el producto a través de DetalleFactura según el nuevo diseño UML:
     * @Mapping(target = "productoId", source = "detalleFactura.producto.id")
     * @Mapping(target = "productoNombre", source = "detalleFactura.producto.nombre")
     */
    @Mapping(target = "productoId", ignore = true)
    @Mapping(target = "productoNombre", ignore = true)
    StockResponse aResponse(Stock stock);

    List<StockResponse> aResponseList(List<Stock> stocks);
}
