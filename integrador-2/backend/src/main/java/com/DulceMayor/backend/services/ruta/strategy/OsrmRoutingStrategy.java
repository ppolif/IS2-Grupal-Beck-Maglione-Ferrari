package com.DulceMayor.backend.services.ruta.strategy;

import com.DulceMayor.backend.entities.persona.PuntoDeVenta;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Estrategia de enrutamiento basada en la API vial de OpenStreetMap (OSRM).
 * Calcula la matriz de distancias reales por calle (asfalto) en lugar de la distancia euclidiana,
 * con fallback transparente a la fórmula de Haversine ante cortes de red o timeouts.
 *
 * el backend le pide a OpenStreetMap una matriz de distancias por calle en metros:
 *
 * "Tengo la Sede Central y 4 comercios. Decime cuántos metros de asfalto hay entre cada uno".
 *
 * Con esos metros reales, el backend ordena la lista de paradas y guarda en la base de datos:
 * Parada 1: Kiosco El Sol
 * Parada 2: Almacén Don Pepe
 * Parada 3: Drugstore Mitre
 * Al frontend solo le envía la lista ordenada (un JSON liviano con 4 elementos).
 *
 * En OSMMap.jsx, el frontend toma esa lista ya ordenada que le dio el backend y le pide a OpenStreetMap:
 *
 * "Ya sé que el orden es Central
 * →
 * → El Sol
 * →
 * → Don Pepe. Ahora dame las coordenadas calle por calle para dibujarle la línea al chofer".
 *
 * OpenStreetMap le devuelve los miles de puntitos que van doblando por las esquinas y avenidas de Mendoza, y Leaflet los pinta sobre el mapa.
 */
@Component
@Primary
public class OsrmRoutingStrategy implements RutaOptimizerStrategy {

    private static final Logger log = LoggerFactory.getLogger(OsrmRoutingStrategy.class);

    private final NearestNeighborStrategy fallbackStrategy;
    private final RestClient restClient;
    private final String osrmBaseUrl;

    public OsrmRoutingStrategy(
            NearestNeighborStrategy fallbackStrategy,
            @Value("${app.osm.osrm-url:https://router.project-osrm.org}") String osrmBaseUrl) {
        this.fallbackStrategy = fallbackStrategy;
        this.osrmBaseUrl = osrmBaseUrl.endsWith("/") ? osrmBaseUrl.substring(0, osrmBaseUrl.length() - 1) : osrmBaseUrl;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(3000);

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public List<PuntoDeVenta> optimizarRecorrido(double latitudInicial, double longitudInicial, List<PuntoDeVenta> paradas) {
        if (paradas == null || paradas.size() <= 1) {
            return paradas != null ? new ArrayList<>(paradas) : Collections.emptyList();
        }

        try {
            // Construir URL: /table/v1/driving/{lonOrigen},{latOrigen};{lon1},{lat1};...
            StringBuilder coords = new StringBuilder();
            coords.append(longitudInicial).append(",").append(latitudInicial);

            for (PuntoDeVenta p : paradas) {
                if (p.getDireccion() != null && p.getDireccion().getLatitud() != null && p.getDireccion().getLongitud() != null) {
                    coords.append(";")
                            .append(p.getDireccion().getLongitud()).append(",")
                            .append(p.getDireccion().getLatitud());
                }
            }

            String url = String.format("%s/table/v1/driving/%s?annotations=distance", osrmBaseUrl, coords);

            OsrmTableResponse response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(OsrmTableResponse.class);

            if (response != null && "Ok".equalsIgnoreCase(response.code()) && response.distances() != null) {
                return ordenarPorMatrizDistancias(paradas, response.distances());
            }
        } catch (Exception e) {
            log.warn("Fallo o timeout al consultar la API de OpenStreetMap (OSRM). Conmutando a fallback Haversine: {}", e.getMessage());
        }

        return fallbackStrategy.optimizarRecorrido(latitudInicial, longitudInicial, paradas);
    }

    @Override
    public double calcularDistanciaHaversine(double lat1, double lon1, double lat2, double lon2) {
        try {
            String url = String.format("%s/route/v1/driving/%f,%f;%f,%f?overview=false", osrmBaseUrl, lon1, lat1, lon2, lat2);
            OsrmRouteResponse res = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(OsrmRouteResponse.class);

            if (res != null && "Ok".equalsIgnoreCase(res.code()) && res.routes() != null && !res.routes().isEmpty()) {
                return res.routes().get(0).distance();
            }
        } catch (Exception e) {
            log.debug("Fallback local para distancia entre ({}, {}) y ({}, {}): {}", lat1, lon1, lat2, lon2, e.getMessage());
        }

        return fallbackStrategy.calcularDistanciaHaversine(lat1, lon1, lat2, lon2);
    }

    private List<PuntoDeVenta> ordenarPorMatrizDistancias(List<PuntoDeVenta> paradas, double[][] distances) {
        List<PuntoDeVenta> ordenados = new ArrayList<>(paradas.size());
        boolean[] visitados = new boolean[paradas.size()];

        int puntoActualIndiceEnMatriz = 0; // 0 representa el origen (sucursal / central)

        for (int i = 0; i < paradas.size(); i++) {
            int mejorIndiceParada = -1;
            double menorDistancia = Double.MAX_VALUE;

            for (int j = 0; j < paradas.size(); j++) {
                if (!visitados[j]) {
                    // En la matriz de OSRM, las paradas están desplazadas +1 respecto al origen (0)
                    int destinoIndiceEnMatriz = j + 1;
                    if (puntoActualIndiceEnMatriz < distances.length && destinoIndiceEnMatriz < distances[puntoActualIndiceEnMatriz].length) {
                        double distancia = distances[puntoActualIndiceEnMatriz][destinoIndiceEnMatriz];
                        if (distancia < menorDistancia) {
                            menorDistancia = distancia;
                            mejorIndiceParada = j;
                        }
                    }
                }
            }

            if (mejorIndiceParada != -1) {
                visitados[mejorIndiceParada] = true;
                ordenados.add(paradas.get(mejorIndiceParada));
                puntoActualIndiceEnMatriz = mejorIndiceParada + 1;
            }
        }

        return ordenados;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record OsrmTableResponse(String code, double[][] distances) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record OsrmRouteResponse(String code, List<OsrmRoute> routes) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record OsrmRoute(double distance, double duration) {}
}
