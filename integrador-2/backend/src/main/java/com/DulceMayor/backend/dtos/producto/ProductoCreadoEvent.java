package com.DulceMayor.backend.dtos.producto;

import com.DulceMayor.backend.services.producto.ProductoService;
import com.DulceMayor.backend.services.producto.StockService;
import com.DulceMayor.backend.services.producto.VigenciaPrecioService;

/**
 * Evento de dominio emitido tras la creación exitosa de un producto en el catálogo.
 *
 * <p><b>Patrón GoF aplicado: Observer (Comportamiento)</b></p>
 * <ul>
 *   <li><b>Participante GoF:</b> Event / State notification (Notificación de Evento y Estado).</li>
 *   <li><b>Sujeto emisor:</b> {@link ProductoService}.</li>
 *   <li><b>Observadores receptores:</b>
 *     <ul>
 *       <li>{@link StockService}: genera el registro inicial en el historial de stock.</li>
 *       <li>{@link VigenciaPrecioService}: genera la vigencia de precio activa inicial.</li>
 *     </ul>
 *   </li>
 *   <li><b>Propósito:</b> Desacoplar completamente los módulos de Stock y Precios respecto al módulo
 *       de Productos, garantizando que el sujeto emita el cambio de estado sin mantener acoplamiento
 *       ni dependencias hacia sus observadores.</li>
 * </ul>
 */
public record ProductoCreadoEvent(
        String productoId,
        Double precioInicial,
        Integer stockInicial
) {
}
