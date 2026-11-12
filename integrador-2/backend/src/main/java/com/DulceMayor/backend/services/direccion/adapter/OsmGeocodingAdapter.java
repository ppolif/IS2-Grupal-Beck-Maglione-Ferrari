package com.DulceMayor.backend.services.direccion.adapter;

import com.DulceMayor.backend.entities.direccion.Departamento;
import com.DulceMayor.backend.entities.direccion.Localidad;
import com.DulceMayor.backend.entities.direccion.Provincia;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Adaptador concreto que integra el geocodificador de OpenStreetMap (Nominatim) junto con el
 * servicio {@code /nearest/v1/driving/} de OSRM para convertir direcciones reales de Mendoza
 * (calle, numeración y localidad) en coordenadas exactas sobre la red vial.
 */
@Component
public class OsmGeocodingAdapter implements GeocodingPort {

    private static final Logger log = LoggerFactory.getLogger(OsmGeocodingAdapter.class);
    private static final String USER_AGENT = "DulceMayor-Logistica-Mendoza/1.0 (utn-frm-is2)";

    private static final double DEFAULT_LAT_MENDOZA = -32.889458;
    private static final double DEFAULT_LON_MENDOZA = -68.845839;

    private final RestClient restClient;
    private final String nominatimBaseUrl;
    private final String osrmBaseUrl;
    private final Map<String, double[]> cacheCoordenadas = new ConcurrentHashMap<>();

    public OsmGeocodingAdapter(
            @Value("${app.osm.nominatim-url:https://nominatim.openstreetmap.org}") String nominatimBaseUrl,
            @Value("${app.osm.osrm-url:https://router.project-osrm.org}") String osrmBaseUrl) {
        this.nominatimBaseUrl = quitarBarraFinal(nominatimBaseUrl);
        this.osrmBaseUrl = quitarBarraFinal(osrmBaseUrl);

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(4000);
        requestFactory.setReadTimeout(4000);

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .defaultHeader("User-Agent", USER_AGENT)
                .defaultHeader("Accept-Language", "es-AR,es;q=0.9")
                .build();
    }

    @Override
    public double[] geocodificarDireccion(String calle, String numeracion, Localidad localidad) {
        String calleLimpia = normalizarCalle(calle);
        String alturaLimpia = normalizarNumeracion(numeracion);
        String nombreLocalidad = localidad != null && localidad.getNombre() != null
                ? localidad.getNombre().trim()
                : "Ciudad de Mendoza";

        Departamento depto = localidad != null ? localidad.getDepartamento() : null;
        String nombreDepartamento = depto != null && depto.getNombre() != null
                ? depto.getNombre().trim()
                : nombreLocalidad;

        Provincia prov = depto != null ? depto.getProvincia() : null;
        String nombreProvincia = prov != null && prov.getNombre() != null
                ? prov.getNombre().trim()
                : "Mendoza";

        String claveCache = String.format("%s|%s|%s|%s|%s",
                calleLimpia.toLowerCase(Locale.ROOT),
                alturaLimpia.toLowerCase(Locale.ROOT),
                nombreLocalidad.toLowerCase(Locale.ROOT),
                nombreDepartamento.toLowerCase(Locale.ROOT),
                nombreProvincia.toLowerCase(Locale.ROOT));

        double[] enCache = cacheCoordenadas.get(claveCache);
        if (enCache != null) {
            return new double[]{enCache[0], enCache[1]};
        }

        double[] coords = resolverEnNominatim(calleLimpia, alturaLimpia, nombreLocalidad, nombreDepartamento, nombreProvincia);
        if (coords == null) {
            coords = obtenerCoordenadasRespaldoPorZona(nombreLocalidad, nombreDepartamento);
        }

        double[] ajustadas = ajustarARedVialOsrm(coords[0], coords[1]);
        cacheCoordenadas.put(claveCache, ajustadas);
        return new double[]{ajustadas[0], ajustadas[1]};
    }

    @Override
    public double[] ajustarARedVialOsrm(double latitud, double longitud) {
        try {
            String url = String.format(Locale.US, "%s/nearest/v1/driving/%.6f,%.6f?number=1",
                    osrmBaseUrl, longitud, latitud);
            OsrmNearestResponse response = restClient.get()
                    .uri(URI.create(url))
                    .retrieve()
                    .body(OsrmNearestResponse.class);

            if (response != null
                    && "Ok".equalsIgnoreCase(response.code())
                    && response.waypoints() != null
                    && !response.waypoints().isEmpty()) {
                double[] loc = response.waypoints().get(0).location();
                if (loc != null && loc.length >= 2) {
                    // En OSRM location es [longitud, latitud]
                    return new double[]{loc[1], loc[0]};
                }
            }
        } catch (Exception e) {
            log.debug("No se pudo ajustar coordenada ({}, {}) en OSRM /nearest: {}", latitud, longitud, e.getMessage());
        }
        return new double[]{latitud, longitud};
    }

    private double[] resolverEnNominatim(
            String calle,
            String numeracion,
            String localidad,
            String departamento,
            String provincia) {

        String streetQuery = numeracion.isEmpty() ? calle : (numeracion + " " + calle);
        String ciudadParaBusqueda = normalizarCiudadMendoza(localidad, departamento);

        // 1. Búsqueda estructurada con calle + altura + localidad/departamento
        if (!calle.isEmpty() && !calle.equalsIgnoreCase("Gran Mendoza")) {
            double[] intento1 = consultarNominatimEstructurado(streetQuery, ciudadParaBusqueda, provincia);
            if (intento1 != null) {
                return intento1;
            }

            // 2. Si la localidad difiere del departamento (ej. Villa Nueva vs Guaymallén), probar por departamento
            if (!ciudadParaBusqueda.equalsIgnoreCase(departamento)) {
                double[] intento2 = consultarNominatimEstructurado(streetQuery, departamento, provincia);
                if (intento2 != null) {
                    return intento2;
                }
            }

            // 3. Búsqueda libre "calle numeracion, departamento, Mendoza, Argentina"
            String qConAltura = numeracion.isEmpty()
                    ? String.format("%s, %s, %s, Argentina", calle, departamento, provincia)
                    : String.format("%s %s, %s, %s, Argentina", calle, numeracion, departamento, provincia);
            double[] intento3 = consultarNominatimLibre(qConAltura);
            if (intento3 != null) {
                return intento3;
            }

            // 4. Búsqueda de la calle sin altura dentro del departamento/localidad
            if (!numeracion.isEmpty()) {
                String qSoloCalle = String.format("%s, %s, %s, Argentina", calle, departamento, provincia);
                double[] intento4 = consultarNominatimLibre(qSoloCalle);
                if (intento4 != null) {
                    return intento4;
                }
            }
        }

        // 5. Fallback al centro de la localidad/departamento en OpenStreetMap
        String qZona = String.format("%s, %s, %s, Argentina", localidad, departamento, provincia);
        return consultarNominatimLibre(qZona);
    }

    private double[] consultarNominatimEstructurado(String street, String city, String state) {
        try {
            URI uri = UriComponentsBuilder.fromUriString(nominatimBaseUrl + "/search")
                    .queryParam("street", street)
                    .queryParam("city", city)
                    .queryParam("state", state)
                    .queryParam("country", "Argentina")
                    .queryParam("countrycodes", "ar")
                    .queryParam("format", "jsonv2")
                    .queryParam("limit", 1)
                    .encode(StandardCharsets.UTF_8)
                    .build()
                    .toUri();

            NominatimPlace[] places = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(NominatimPlace[].class);

            if (places != null && places.length > 0 && places[0].lat() != null && places[0].lon() != null) {
                double lat = Double.parseDouble(places[0].lat());
                double lon = Double.parseDouble(places[0].lon());
                if (esCoordenadaValidaMendoza(lat, lon)) {
                    log.info("Dirección geocodificada en OSM (estructurada): '{}' ({}) -> [{}, {}]", street, city, lat, lon);
                    return new double[]{lat, lon};
                }
            }
        } catch (Exception e) {
            log.debug("Fallo en consulta estructurada Nominatim para '{}', '{}': {}", street, city, e.getMessage());
        }
        return null;
    }

    private double[] consultarNominatimLibre(String query) {
        try {
            URI uri = UriComponentsBuilder.fromUriString(nominatimBaseUrl + "/search")
                    .queryParam("q", query)
                    .queryParam("countrycodes", "ar")
                    .queryParam("format", "jsonv2")
                    .queryParam("limit", 1)
                    .encode(StandardCharsets.UTF_8)
                    .build()
                    .toUri();

            NominatimPlace[] places = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(NominatimPlace[].class);

            if (places != null && places.length > 0 && places[0].lat() != null && places[0].lon() != null) {
                double lat = Double.parseDouble(places[0].lat());
                double lon = Double.parseDouble(places[0].lon());
                if (esCoordenadaValidaMendoza(lat, lon)) {
                    log.info("Dirección geocodificada en OSM (libre): '{}' -> [{}, {}]", query, lat, lon);
                    return new double[]{lat, lon};
                }
            }
        } catch (Exception e) {
            log.debug("Fallo en consulta libre Nominatim para '{}': {}", query, e.getMessage());
        }
        return null;
    }

    private boolean esCoordenadaValidaMendoza(double lat, double lon) {
        // Rango geográfico amplio de la provincia de Mendoza para descartar falsos positivos fuera de la región
        return lat >= -37.6 && lat <= -31.9 && lon >= -70.6 && lon <= -66.5;
    }

    private String normalizarCalle(String calle) {
        if (calle == null) {
            return "";
        }
        String limpia = calle.trim()
                .replaceAll("(?i)^Av\\.?\\s+", "Avenida ")
                .replaceAll("(?i)^Bv\\.?\\s+", "Boulevard ")
                .replaceAll("(?i)\\bGral\\.?\\b", "General")
                .replaceAll("(?i)\\bCnel\\.?\\b", "Coronel")
                .replaceAll("(?i)\\bDr\\.?\\b", "Doctor")
                .replaceAll("(?i)\\bPte\\.?\\b", "Presidente")
                .replaceAll("(?i)\\b Alte\\.?\\b", " Almirante")
                .replaceAll("\\s+", " ")
                .trim();
        return limpia;
    }

    private String normalizarNumeracion(String numeracion) {
        if (numeracion == null) {
            return "";
        }
        String limpia = numeracion.trim();
        if (limpia.equalsIgnoreCase("S/N") || limpia.equalsIgnoreCase("SN") || limpia.equals("0")) {
            return "";
        }
        return limpia.replaceAll("[^0-9]", "");
    }

    private String normalizarCiudadMendoza(String localidad, String departamento) {
        if (localidad == null || localidad.isBlank()) {
            return "Mendoza";
        }
        if (localidad.equalsIgnoreCase("Ciudad de Mendoza") || localidad.equalsIgnoreCase("Ciudad") || localidad.equalsIgnoreCase("Capital")) {
            return "Mendoza";
        }
        return localidad;
    }

    private double[] obtenerCoordenadasRespaldoPorZona(String localidad, String departamento) {
        String zona = (localidad + " " + departamento).toLowerCase(Locale.ROOT);
        if (zona.contains("godoy cruz")) {
            return new double[]{-32.925000, -68.844000};
        }
        if (zona.contains("guaymall") || zona.contains("villa nueva") || zona.contains("dorrego") || zona.contains("san jos")) {
            return new double[]{-32.898000, -68.785000};
        }
        if (zona.contains("las heras") || zona.contains("plumerillo")) {
            return new double[]{-32.852000, -68.835000};
        }
        if (zona.contains("maip") || zona.contains("guti")) {
            return new double[]{-32.977000, -68.788000};
        }
        if (zona.contains("luj") || zona.contains("chacras")) {
            return new double[]{-33.038000, -68.879000};
        }
        if (zona.contains("san mart")) {
            return new double[]{-33.081000, -68.468000};
        }
        return new double[]{DEFAULT_LAT_MENDOZA, DEFAULT_LON_MENDOZA};
    }

    private static String quitarBarraFinal(String url) {
        return url != null && url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record NominatimPlace(String lat, String lon, String display_name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record OsrmNearestResponse(String code, List<OsrmNearestWaypoint> waypoints) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record OsrmNearestWaypoint(double[] location, double distance, String name) {}
}
