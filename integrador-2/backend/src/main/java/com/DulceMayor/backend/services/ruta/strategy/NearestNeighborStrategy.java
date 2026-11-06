package com.DulceMayor.backend.services.ruta.strategy;

import com.DulceMayor.backend.entities.direccion.Direccion;
import com.DulceMayor.backend.entities.persona.PuntoDeVenta;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Implementación concreta del patrón Strategy que resuelve el problema del viajante (TSP)
 * mediante la heurística constructiva del <b>Vecino Más Cercano (Nearest Neighbor)</b>
 * utilizando la <b>Fórmula de Haversine</b> para calcular la distancia ortodrómica sobre la superficie terrestre,
 * conforme a {@code 02-reglas-negocio.md} (§6) y {@code 03-arquitectura.md} (§4.2).
 *
 * <p><b>Uso de datos geográficos compatibles con OpenStreetMap:</b></p>
 * <p>OpenStreetMap (OSM) y sus visores estándar (Leaflet, MapLibre, etc.) trabajan nativamente con el sistema
 * de coordenadas geodésicas <b>WGS84 (EPSG:4326)</b>, compuestas por parejas de latitud y longitud decimales.
 * Este componente consume directamente las coordenadas provistas en {@link Direccion#getLatitud()} y
 * {@link Direccion#getLongitud()}, las cuales alimentan los marcadores, trazados y polilíneas renderizados
 * sobre los mapas de OpenStreetMap en el frontend móvil y web del sistema.</p>
 *
 * <p><b>Algoritmo del Vecino Más Cercano (Nearest Neighbor):</b></p>
 * <ol>
 *   <li>Se parte desde una coordenada de origen {@code (latitudInicial, longitudInicial)} (sucursal o central).</li>
 *   <li>De todos los puntos de venta no visitados, se calcula la distancia Haversine a la posición actual y se
 *       selecciona el punto que presente la menor distancia euclidiana/geodésica.</li>
 *   <li>Ese punto se agrega a la secuencia ordenada y se remueve del conjunto de candidatos pendientes.</li>
 *   <li>La posición actual se actualiza a las coordenadas del punto recién elegido.</li>
 *   <li>Se itera hasta que todos los puntos hayan sido incorporados a la ruta ordenada.</li>
 * </ol>
 *
 * <p><b>Fórmula de Haversine:</b></p>
 * Dado el radio medio terrestre \(R \approx 6.371.000\text{ metros}\):
 * \[a = \sin^2\left(\frac{\Delta \varphi}{2}\right) + \cos(\varphi_1) \cdot \cos(\varphi_2) \cdot \sin^2\left(\frac{\Delta \lambda}{2}\right)\]
 * \[c = 2 \cdot \text{atan2}\left(\sqrt{a}, \sqrt{1 - a}\right)\]
 * \[d = R \cdot c\]
 * donde \(\varphi\) representa la latitud y \(\lambda\) la longitud en radianes.
 */
@Component
public class NearestNeighborStrategy implements RutaOptimizerStrategy {

    /**
     * Radio medio aproximado del planeta Tierra en metros.
     */
    private static final double RADIO_TIERRA_METROS = 6371000.0;

    /**
     * Ordena la lista de paradas buscando greedy (vorazmente) en cada iteración el punto más cercano.
     * Si algún punto no posee coordenadas configuradas, se coloca al final de la ruta para no interrumpir el cálculo.
     *
     * @param latitudInicial  Latitud del punto de partida.
     * @param longitudInicial Longitud del punto de partida.
     * @param paradas         Lista original de puntos de venta a visitar.
     * @return Lista ordenada con el recorrido optimizado según vecino más cercano.
     */
    @Override
    public List<PuntoDeVenta> optimizarRecorrido(
            double latitudInicial,
            double longitudInicial,
            List<PuntoDeVenta> paradas) {
        if (paradas == null || paradas.isEmpty()) {
            return new ArrayList<>();
        }

        List<PuntoDeVenta> candidatos = new ArrayList<>(paradas);
        List<PuntoDeVenta> recorridoOrdenado = new ArrayList<>(paradas.size());

        double latActual = latitudInicial;
        double lonActual = longitudInicial;

        while (!candidatos.isEmpty()) {
            int mejorIndice = -1;
            double menorDistancia = Double.MAX_VALUE;

            for (int i = 0; i < candidatos.size(); i++) {
                PuntoDeVenta candidato = candidatos.get(i);
                Direccion dir = candidato.getDireccion();

                if (dir == null || dir.getLatitud() == 0.0 && dir.getLongitud() == 0.0) {
                    continue;
                }

                double distancia = calcularDistanciaHaversine(latActual, lonActual, dir.getLatitud(), dir.getLongitud());
                if (distancia < menorDistancia) {
                    menorDistancia = distancia;
                    mejorIndice = i;
                }
            }

            if (mejorIndice == -1) {
                // Los puntos restantes carecen de geolocalización; se anexan al final tal como vienen
                recorridoOrdenado.addAll(candidatos);
                break;
            }

            PuntoDeVenta siguientePunto = candidatos.remove(mejorIndice);
            recorridoOrdenado.add(siguientePunto);

            if (siguientePunto.getDireccion() != null) {
                latActual = siguientePunto.getDireccion().getLatitud();
                lonActual = siguientePunto.getDireccion().getLongitud();
            }
        }

        return recorridoOrdenado;
    }

    /**
     * Calcula la distancia ortodrómica en metros entre dos pares de coordenadas geográficas (latitud/longitud)
     * compatibles con la proyección y los datos espaciales de OpenStreetMap.
     *
     * @param lat1 Latitud del punto origen (grados decimales).
     * @param lon1 Longitud del punto origen (grados decimales).
     * @param lat2 Latitud del punto destino (grados decimales).
     * @param lon2 Longitud del punto destino (grados decimales).
     * @return Distancia en línea recta sobre la superficie terrestre en metros.
     */
    public double calcularDistanciaHaversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(phi1) * Math.cos(phi2)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return RADIO_TIERRA_METROS * c;
    }
}

