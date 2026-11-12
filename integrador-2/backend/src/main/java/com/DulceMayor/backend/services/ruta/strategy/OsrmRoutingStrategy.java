package com.DulceMayor.backend.services.ruta.strategy;

import com.DulceMayor.backend.entities.direccion.Direccion;
import com.DulceMayor.backend.entities.persona.PuntoDeVenta;
import com.DulceMayor.backend.services.direccion.adapter.GeocodingPort;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Estrategia de enrutamiento que delega íntegramente a OpenStreetMap y OSRM:
 * <ul>
 *   <li><b>Optimización de Ruta Más Óptima (TSP):</b> Delegada al servicio {@code /trip/v1/driving/} de OSRM
 *       con {@code source=first&roundtrip=false}, donde el propio motor de OSRM resuelve el orden óptimo
 *       de visita sobre la red real de calles y sentidos de circulación de Mendoza.</li>
 *   <li><b>Matriz de Distancias Viales:</b> Delegada al servicio {@code /table/v1/driving/} de OSRM para
 *       balanceo de carga de distribuidores y asignación de sucursal más cercana.</li>
 *   <li><b>Distancia entre Pares:</b> Delegada al servicio {@code /route/v1/driving/} de OSRM.</li>
 * </ul>
 */
@Component
@Primary
public class OsrmRoutingStrategy implements RutaOptimizerStrategy {

    private static final Logger log = LoggerFactory.getLogger(OsrmRoutingStrategy.class);

    private final RestClient restClient;
    private final String osrmBaseUrl;
    private final GeocodingPort geocodingPort;

    public OsrmRoutingStrategy(
            @Value("${app.osm.osrm-url:https://router.project-osrm.org}") String osrmBaseUrl,
            GeocodingPort geocodingPort) {
        this.osrmBaseUrl = osrmBaseUrl != null && osrmBaseUrl.endsWith("/")
                ? osrmBaseUrl.substring(0, osrmBaseUrl.length() - 1)
                : osrmBaseUrl;
        this.geocodingPort = geocodingPort;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(5000);

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public List<PuntoDeVenta> optimizarRecorrido(double latitudInicial, double longitudInicial, List<PuntoDeVenta> paradas) {
        if (paradas == null || paradas.isEmpty()) {
            return Collections.emptyList();
        }

        List<PuntoDeVenta> paradasConCoordenadas = new ArrayList<>(paradas.size());
        List<PuntoDeVenta> paradasSinDireccion = new ArrayList<>();

        for (PuntoDeVenta p : paradas) {
            Direccion dir = p.getDireccion();
            if (dir == null) {
                paradasSinDireccion.add(p);
                continue;
            }
            asegurarCoordenadasEnParada(dir);
            if (dir.getLatitud() != null && dir.getLongitud() != null
                    && (dir.getLatitud() != 0.0 || dir.getLongitud() != 0.0)) {
                paradasConCoordenadas.add(p);
            } else {
                paradasSinDireccion.add(p);
            }
        }

        if (paradasConCoordenadas.size() <= 1) {
            List<PuntoDeVenta> resultadoSimple = new ArrayList<>(paradasConCoordenadas);
            resultadoSimple.addAll(paradasSinDireccion);
            return resultadoSimple;
        }

        // Construir coordenadas en formato OSRM: {lonOrigen},{latOrigen};{lon1},{lat1};...
        StringBuilder coords = new StringBuilder();
        coords.append(String.format(Locale.US, "%.6f,%.6f", longitudInicial, latitudInicial));

        for (PuntoDeVenta p : paradasConCoordenadas) {
            Direccion dir = p.getDireccion();
            coords.append(";")
                    .append(String.format(Locale.US, "%.6f,%.6f", dir.getLongitud(), dir.getLatitud()));
        }

        try {
            // Delegar el cálculo completo del viaje óptimo (TSP) al servicio /trip/v1/driving/ de OSRM
            String tripUrl = String.format(
                    "%s/trip/v1/driving/%s?source=first&roundtrip=false&overview=false",
                    osrmBaseUrl,
                    coords
            );

            OsrmTripResponse response = restClient.get()
                    .uri(URI.create(tripUrl))
                    .retrieve()
                    .body(OsrmTripResponse.class);

            if (response != null
                    && "Ok".equalsIgnoreCase(response.code())
                    && response.waypoints() != null
                    && response.waypoints().size() == paradasConCoordenadas.size() + 1) {

                List<Integer> indicesParadas = new ArrayList<>(paradasConCoordenadas.size());
                for (int i = 0; i < paradasConCoordenadas.size(); i++) {
                    indicesParadas.add(i);
                }

                // En OSRM Trip, waypoints.get(0) corresponde al origen (source=first) y
                // waypoints.get(i + 1) corresponde a paradasConCoordenadas.get(i), indicando en waypoint_index
                // su posición exacta en el recorrido más óptimo calculado por OSRM.
                indicesParadas.sort(Comparator
                        .comparingInt((Integer i) -> response.waypoints().get(i + 1).trips_index())
                        .thenComparingInt(i -> response.waypoints().get(i + 1).waypoint_index()));

                List<PuntoDeVenta> ordenadosPorOsrm = new ArrayList<>(paradas.size());
                for (int idx : indicesParadas) {
                    ordenadosPorOsrm.add(paradasConCoordenadas.get(idx));
                }
                ordenadosPorOsrm.addAll(paradasSinDireccion);
                return ordenadosPorOsrm;
            }
        } catch (Exception e) {
            log.warn("Error al consultar OSRM /trip/v1/driving/ ({}): {}", osrmBaseUrl, e.getMessage());
        }

        List<PuntoDeVenta> respaldo = new ArrayList<>(paradasConCoordenadas);
        respaldo.addAll(paradasSinDireccion);
        return respaldo;
    }

    @Override
    public double calcularDistanciaVial(double lat1, double lon1, double lat2, double lon2) {
        try {
            String url = String.format(
                    Locale.US,
                    "%s/route/v1/driving/%.6f,%.6f;%.6f,%.6f?overview=false",
                    osrmBaseUrl, lon1, lat1, lon2, lat2
            );
            OsrmRouteResponse res = restClient.get()
                    .uri(URI.create(url))
                    .retrieve()
                    .body(OsrmRouteResponse.class);

            if (res != null && "Ok".equalsIgnoreCase(res.code()) && res.routes() != null && !res.routes().isEmpty()) {
                return res.routes().get(0).distance();
            }
        } catch (Exception e) {
            log.debug("No se pudo obtener distancia vial OSRM entre ({}, {}) y ({}, {}): {}", lat1, lon1, lat2, lon2, e.getMessage());
        }
        return Double.MAX_VALUE;
    }

    @Override
    public double[] calcularDistanciasDesdeOrigen(double latOrigen, double lonOrigen, List<double[]> destinos) {
        if (destinos == null || destinos.isEmpty()) {
            return new double[0];
        }

        double[] resultado = new double[destinos.size()];
        StringBuilder coords = new StringBuilder();
        coords.append(String.format(Locale.US, "%.6f,%.6f", lonOrigen, latOrigen));

        for (double[] d : destinos) {
            coords.append(";").append(String.format(Locale.US, "%.6f,%.6f", d[1], d[0]));
        }

        try {
            String url = String.format(
                    "%s/table/v1/driving/%s?sources=0&annotations=distance",
                    osrmBaseUrl,
                    coords
            );
            OsrmTableResponse response = restClient.get()
                    .uri(URI.create(url))
                    .retrieve()
                    .body(OsrmTableResponse.class);

            if (response != null
                    && "Ok".equalsIgnoreCase(response.code())
                    && response.distances() != null
                    && response.distances().length > 0
                    && response.distances()[0].length >= destinos.size() + 1) {
                for (int i = 0; i < destinos.size(); i++) {
                    Double valor = response.distances()[0][i + 1];
                    resultado[i] = valor != null ? valor : Double.MAX_VALUE;
                }
                return resultado;
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener tabla de distancias desde origen en OSRM /table/v1/driving/: {}", e.getMessage());
        }

        for (int i = 0; i < destinos.size(); i++) {
            resultado[i] = calcularDistanciaVial(latOrigen, lonOrigen, destinos.get(i)[0], destinos.get(i)[1]);
        }
        return resultado;
    }

    @Override
    public double[][] calcularMatrizDistancias(List<double[]> coordenadas) {
        if (coordenadas == null || coordenadas.isEmpty()) {
            return new double[0][0];
        }
        int n = coordenadas.size();
        if (n == 1) {
            return new double[][]{{0.0}};
        }

        StringBuilder coords = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) {
                coords.append(";");
            }
            coords.append(String.format(Locale.US, "%.6f,%.6f", coordenadas.get(i)[1], coordenadas.get(i)[0]));
        }

        try {
            String url = String.format(
                    "%s/table/v1/driving/%s?annotations=distance",
                    osrmBaseUrl,
                    coords
            );
            OsrmTableResponse response = restClient.get()
                    .uri(URI.create(url))
                    .retrieve()
                    .body(OsrmTableResponse.class);

            if (response != null
                    && "Ok".equalsIgnoreCase(response.code())
                    && response.distances() != null
                    && response.distances().length == n) {
                double[][] matriz = new double[n][n];
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        Double val = response.distances()[i][j];
                        matriz[i][j] = val != null ? val : Double.MAX_VALUE;
                    }
                }
                return matriz;
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener matriz completa en OSRM /table/v1/driving/: {}", e.getMessage());
        }

        double[][] fallback = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                fallback[i][j] = i == j ? 0.0 : Double.MAX_VALUE;
            }
        }
        return fallback;
    }

    private void asegurarCoordenadasEnParada(Direccion dir) {
        if (geocodingPort == null) {
            return;
        }
        boolean sinCoordenadas = dir.getLatitud() == null
                || dir.getLongitud() == null
                || (dir.getLatitud() == 0.0 && dir.getLongitud() == 0.0);
        if (sinCoordenadas) {
            double[] coords = geocodingPort.geocodificarDireccion(
                    dir.getCalle(),
                    dir.getNumeracion(),
                    dir.getLocalidad()
            );
            dir.setLatitud(coords[0]);
            dir.setLongitud(coords[1]);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record OsrmTripResponse(String code, List<OsrmTripWaypoint> waypoints, List<OsrmRoute> trips) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record OsrmTripWaypoint(int waypoint_index, int trips_index, double[] location, double distance) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record OsrmTableResponse(String code, Double[][] distances) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record OsrmRouteResponse(String code, List<OsrmRoute> routes) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record OsrmRoute(double distance, double duration) {}
}
