package com.DulceMayor.backend.services.ruta.strategy;

import com.DulceMayor.backend.entities.persona.PuntoDeVenta;
import java.util.List;

/**
 * Interfaz Strategy que desacopla el algoritmo de cálculo y optimización del recorrido geográfico
 * de las rutas de preventa y distribución, conforme a {@code 03-arquitectura.md} (§4.2).
 *
 * <p><b>Patrón GoF aplicado: Strategy (Comportamiento)</b></p>
 * <ul>
 *   <li><b>Contexto:</b> Servicios de generación de ruta ({@code RutaPreventorService} y {@code RutaDistribuidorService}).</li>
 *   <li><b>Strategy:</b> Esta interfaz ({@code RutaOptimizerStrategy}).</li>
 *   <li><b>Estrategia concreta por defecto:</b> {@link NearestNeighborStrategy}, que utiliza la fórmula
 *       ortodrómica de Haversine para encontrar secuencialmente el vecino más cercano a partir de coordenadas geodésicas.</li>
 *   <li><b>Ventaja de arquitectura:</b> Permite en el futuro reemplazar o sumar motores externos de optimización
 *       logística (como Google OR-Tools, GraphHopper o el motor de rutas de OpenStreetMap / OSRM) sin modificar
 *       la lógica transaccional de los servicios del negocio.</li>
 * </ul>
 */
public interface RutaOptimizerStrategy {

    /**
     * Ordena una lista de puntos de venta de forma óptima a partir de una coordenada inicial de partida.
     *
     * @param latitudInicial  Latitud del punto de salida (ej. sucursal o sede central).
     * @param longitudInicial Longitud del punto de salida.
     * @param paradas         Colección de puntos de venta a secuenciar.
     * @return Lista reordenada de puntos de venta representando el itinerario óptimo sugerido.
     */
    List<PuntoDeVenta> optimizarRecorrido(double latitudInicial, double longitudInicial, List<PuntoDeVenta> paradas);

    /**
     * Calcula la distancia ortodrómica en metros entre dos pares de coordenadas (fórmula de Haversine).
     *
     * @param lat1 Latitud origen en grados decimales.
     * @param lon1 Longitud origen en grados decimales.
     * @param lat2 Latitud destino en grados decimales.
     * @param lon2 Longitud destino en grados decimales.
     * @return Distancia en metros.
     */
    double calcularDistanciaHaversine(double lat1, double lon1, double lat2, double lon2);
}

