package com.DulceMayor.backend.dtos;

/**
 * Evento de dominio emitido tras la creación exitosa de un producto en el catálogo.
 *
 * <p><b>Patrón GoF aplicado: Observer (Comportamiento)</b></p>
 * <ul>
 *   <li><b>Participante GoF:</b> Event / State notification (Notificación de Evento y Estado).</li>
 *   <li><b>Sujeto emisor:</b> {@link com.DulceMayor.backend.services.ProductoService}.</li>
 *   <li><b>Observadores receptores:</b>
 *     <ul>
 *       <li>{@link com.DulceMayor.backend.services.StockService}: genera el registro inicial en el historial de stock.</li>
 *       <li>{@link com.DulceMayor.backend.services.VigenciaPrecioService}: genera la vigencia de precio activa inicial.</li>
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
