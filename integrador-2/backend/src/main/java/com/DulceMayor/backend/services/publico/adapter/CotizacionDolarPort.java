package com.DulceMayor.backend.services.publico.adapter;

import com.DulceMayor.backend.dtos.publico.CotizacionDolarResponse;

/**
 * Interfaz de dominio (Target del patrón Adapter) para obtener la cotización actualizada del dólar.
 *
 * <p><b>Patrón GoF aplicado: Adapter (Estructural)</b></p>
 * <ul>
 *   <li><b>Participante GoF:</b> Target (Interfaz objetivo esperada por el dominio).</li>
 *   <li><b>Justificación:</b> Desacopla la capa de negocio de Dulce Mayor del formato JSON,
 *       protocolo HTTP y proveedor externo de cotizaciones cambiarias.</li>
 * </ul>
 */
public interface CotizacionDolarPort {

    /**
     * Obtiene la cotización actual del dólar estadounidense (compra/venta),
     * utilizando caché de corta duración y valor de respaldo en caso de falla del servicio externo.
     */
    CotizacionDolarResponse obtenerCotizacionActual();
}

