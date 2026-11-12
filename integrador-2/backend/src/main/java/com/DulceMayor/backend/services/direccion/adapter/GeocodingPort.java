package com.DulceMayor.backend.services.direccion.adapter;

import com.DulceMayor.backend.entities.direccion.Localidad;

/**
 * Interfaz de dominio (Target del patrón Adapter) para geocodificar direcciones postales
 * y ajustarlas a la red vial de OpenStreetMap mediante Nominatim y OSRM.
 *
 * <p><b>Patrón GoF aplicado: Adapter (Estructural)</b></p>
 * <ul>
 *   <li><b>Participante GoF:</b> Target (Interfaz esperada por {@code DireccionService} y {@code OsrmRoutingStrategy}).</li>
 *   <li><b>Justificación:</b> Desacopla la lógica de persistencia de direcciones del protocolo HTTP y formato JSON
 *       de los servicios geográficos externos de OpenStreetMap (Nominatim + OSRM Nearest).</li>
 * </ul>
 */
public interface GeocodingPort {

    /**
     * Resuelve las coordenadas geodésicas WGS84 {@code [latitud, longitud]} correspondientes a una calle,
     * numeración y localidad de Mendoza utilizando OpenStreetMap y ajustándolas a la vía navegable más cercana con OSRM.
     *
     * @param calle      Nombre de la calle o avenida.
     * @param numeracion Altura o numeración municipal.
     * @param localidad  Entidad {@link Localidad} con su departamento y provincia asociados.
     * @return Arreglo de dos elementos {@code [latitud, longitud]} en grados decimales.
     */
    double[] geocodificarDireccion(String calle, String numeracion, Localidad localidad);

    /**
     * Ajusta (snapping) un par de coordenadas {@code (latitud, longitud)} al nodo vial navegable
     * más cercano dentro del grafo de calles de OSRM (`/nearest/v1/driving/`).
     *
     * @param latitud  Latitud original en grados decimales.
     * @param longitud Longitud original en grados decimales.
     * @return Arreglo {@code [latitudAjustada, longitudAjustada]} sobre el asfalto de OpenStreetMap.
     */
    double[] ajustarARedVialOsrm(double latitud, double longitud);
}
