package com.DulceMayor.backend.services.ruta.strategy;

import com.DulceMayor.backend.entities.persona.PuntoDeVenta;
import java.util.List;

/**
 * Interfaz Strategy que define las operaciones de cálculo y optimización de rutas viales
 * delegadas al motor de enrutamiento de OpenStreetMap (OSRM).
 *
 * <p><b>Patrón GoF aplicado: Strategy (Comportamiento)</b></p>
 * <ul>
 *   <li><b>Contexto:</b> Servicios de generación de ruta ({@code RutaPreventorService}, {@code RutaDistribuidorService} y {@code EmpleadoService}).</li>
 *   <li><b>Strategy:</b> Esta interfaz ({@code RutaOptimizerStrategy}).</li>
 *   <li><b>Estrategia concreta:</b> {@link OsrmRoutingStrategy}, que delega la resolución completa del
 *       Problema del Viajante (TSP) al servicio {@code /trip/v1/driving/} de OSRM y las matrices de distancia
 *       vial al servicio {@code /table/v1/driving/} de OSRM.</li>
 * </ul>
 */
public interface RutaOptimizerStrategy {

    /**
     * Calcula la secuencia óptima de visita de una lista de puntos de venta partiendo desde una coordenada
     * inicial (sucursal o sede central), delegando la resolución del TSP en el servicio {@code /trip/v1/driving/} de OSRM.
     *
     * @param latitudInicial  Latitud del punto de salida (ej. sucursal o sede central).
     * @param longitudInicial Longitud del punto de salida.
     * @param paradas         Colección de puntos de venta a secuenciar.
     * @return Lista reordenada de puntos de venta según el viaje óptimo calculado por OSRM.
     */
    List<PuntoDeVenta> optimizarRecorrido(double latitudInicial, double longitudInicial, List<PuntoDeVenta> paradas);

    /**
     * Calcula la distancia vial real en metros por calles de OpenStreetMap entre dos coordenadas
     * utilizando el servicio {@code /route/v1/driving/} de OSRM.
     *
     * @param lat1 Latitud origen en grados decimales.
     * @param lon1 Longitud origen en grados decimales.
     * @param lat2 Latitud destino en grados decimales.
     * @param lon2 Longitud destino en grados decimales.
     * @return Distancia vial en metros calculada por OSRM.
     */
    double calcularDistanciaVial(double lat1, double lon1, double lat2, double lon2);

    /**
     * Calcula en una sola consulta a {@code /table/v1/driving/} de OSRM las distancias viales en metros
     * desde un punto de origen hacia múltiples coordenadas de destino.
     *
     * @param latOrigen Latitud del origen.
     * @param lonOrigen Longitud del origen.
     * @param destinos  Lista de pares {@code [latitud, longitud]} de destino.
     * @return Arreglo con la distancia vial en metros desde el origen hacia cada destino en el mismo orden.
     */
    double[] calcularDistanciasDesdeOrigen(double latOrigen, double lonOrigen, List<double[]> destinos);

    /**
     * Calcula en una sola consulta a {@code /table/v1/driving/} de OSRM la matriz completa \(N \times N\)
     * de distancias viales en metros entre todas las coordenadas suministradas.
     *
     * @param coordenadas Lista de pares {@code [latitud, longitud]}.
     * @return Matriz bidimensional de distancias en metros donde {@code matriz[i][j]} es la distancia vial de {@code i} a {@code j}.
     */
    double[][] calcularMatrizDistancias(List<double[]> coordenadas);
}
