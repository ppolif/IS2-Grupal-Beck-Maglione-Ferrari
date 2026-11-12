package com.DulceMayor.backend.services.ruta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.DulceMayor.backend.entities.direccion.Departamento;
import com.DulceMayor.backend.entities.direccion.Direccion;
import com.DulceMayor.backend.entities.direccion.Localidad;
import com.DulceMayor.backend.entities.direccion.Provincia;
import com.DulceMayor.backend.entities.persona.PuntoDeVenta;
import com.DulceMayor.backend.services.direccion.adapter.GeocodingPort;
import com.DulceMayor.backend.services.direccion.adapter.OsmGeocodingAdapter;
import com.DulceMayor.backend.services.ruta.strategy.OsrmRoutingStrategy;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests unitarios de {@link OsrmRoutingStrategy} y {@link OsmGeocodingAdapter} verificando
 * la delegación completa a OSRM (/trip/v1/driving/, /table/v1/driving/, /nearest/v1/driving/) y Nominatim.
 */
class OsrmRoutingStrategyTest {

    private HttpServer mockServer;
    private String baseUrl;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = HttpServer.create(new InetSocketAddress(0), 0);

        // Endpoint simulado de OSRM Trip (/trip/v1/driving/)
        // Invierte el orden de las paradas 1 y 2 mediante waypoint_index (parada 1 -> index 2, parada 2 -> index 1)
        mockServer.createContext("/trip/v1/driving/", exchange -> {
            String json = """
                    {
                      "code": "Ok",
                      "waypoints": [
                        { "waypoint_index": 0, "trips_index": 0, "location": [-68.845839, -32.889458], "distance": 0.0 },
                        { "waypoint_index": 2, "trips_index": 0, "location": [-68.844000, -32.925000], "distance": 1.2 },
                        { "waypoint_index": 1, "trips_index": 0, "location": [-68.838000, -32.885000], "distance": 0.8 }
                      ],
                      "trips": [
                        { "distance": 5420.5, "duration": 610.2 }
                      ]
                    }
                    """;
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });

        // Endpoint simulado de OSRM Table (/table/v1/driving/)
        mockServer.createContext("/table/v1/driving/", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            String json;
            if (query != null && query.contains("sources=0")) {
                json = """
                        {
                          "code": "Ok",
                          "distances": [
                            [0.0, 3200.0, 1450.0]
                          ]
                        }
                        """;
            } else {
                json = """
                        {
                          "code": "Ok",
                          "distances": [
                            [0.0, 1800.0],
                            [1850.0, 0.0]
                          ]
                        }
                        """;
            }
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });

        // Endpoint simulado de OSRM Nearest (/nearest/v1/driving/)
        mockServer.createContext("/nearest/v1/driving/", exchange -> {
            String json = """
                    {
                      "code": "Ok",
                      "waypoints": [
                        { "location": [-68.841122, -32.891234], "distance": 2.1, "name": "Avenida San Martín" }
                      ]
                    }
                    """;
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });

        // Endpoint simulado de Nominatim (/search)
        mockServer.createContext("/search", exchange -> {
            String json = """
                    [
                      {
                        "lat": "-32.891200",
                        "lon": "-68.841100",
                        "display_name": "Avenida San Martín 1450, Ciudad de Mendoza, Mendoza, Argentina"
                      }
                    ]
                    """;
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });

        mockServer.start();
        baseUrl = "http://localhost:" + mockServer.getAddress().getPort();
    }

    @AfterEach
    void tearDown() {
        if (mockServer != null) {
            mockServer.stop(0);
        }
    }

    @Test
    @DisplayName("OsmGeocodingAdapter geocodifica calle y altura en Nominatim y ajusta al carril en OSRM /nearest")
    void geocodificarDireccionConNominatimYOsrmNearest() {
        GeocodingPort geocodingPort = new OsmGeocodingAdapter(baseUrl, baseUrl);

        Provincia mendoza = Provincia.builder().nombre("Mendoza").build();
        Departamento capital = Departamento.builder().nombre("Capital").provincia(mendoza).build();
        Localidad ciudad = Localidad.builder().nombre("Ciudad de Mendoza").departamento(capital).build();

        double[] coords = geocodingPort.geocodificarDireccion("Av. San Martín", "1450", ciudad);

        assertNotNull(coords);
        assertEquals(2, coords.length);
        assertEquals(-32.891234, coords[0], 0.0001);
        assertEquals(-68.841122, coords[1], 0.0001);
    }

    @Test
    @DisplayName("OsrmRoutingStrategy delega el orden óptimo al servicio /trip/v1/driving/ de OSRM y geocodifica comercios sin coordenadas")
    void optimizarRecorridoDelegaEnOsrmTrip() {
        GeocodingPort geocodingPort = new OsmGeocodingAdapter(baseUrl, baseUrl);
        OsrmRoutingStrategy strategy = new OsrmRoutingStrategy(baseUrl, geocodingPort);

        PuntoDeVenta pdvLejano = PuntoDeVenta.builder()
                .id("pdv-1")
                .nombreComercio("Kiosco Godoy Cruz")
                .direccion(Direccion.builder()
                        .calle("Belgrano")
                        .numeracion("1200")
                        .latitud(-32.925000)
                        .longitud(-68.844000)
                        .build())
                .build();

        // Comercio recién dado de alta sin coordenadas iniciales (latitud y longitud null)
        PuntoDeVenta pdvSinCoordenadas = PuntoDeVenta.builder()
                .id("pdv-2")
                .nombreComercio("Almacén San Martín")
                .direccion(Direccion.builder()
                        .calle("Av. San Martín")
                        .numeracion("1450")
                        .latitud(null)
                        .longitud(null)
                        .build())
                .build();

        List<PuntoDeVenta> optimizados = strategy.optimizarRecorrido(
                -32.889458,
                -68.845839,
                List.of(pdvLejano, pdvSinCoordenadas)
        );

        assertEquals(2, optimizados.size());
        // Según waypoint_index de OSRM Trip, pdv-2 va primero (waypoint_index=1) y pdv-1 va segundo (waypoint_index=2)
        assertEquals("pdv-2", optimizados.get(0).getId());
        assertEquals("pdv-1", optimizados.get(1).getId());
        // Verifica que pdvSinCoordenadas fue geocodificado automáticamente y no fue descartado
        assertNotNull(pdvSinCoordenadas.getDireccion().getLatitud());
        assertNotNull(pdvSinCoordenadas.getDireccion().getLongitud());
    }

    @Test
    @DisplayName("OsrmRoutingStrategy calcula distancias desde origen y matriz completa mediante /table/v1/driving/")
    void calcularDistanciasYMatrizConOsrmTable() {
        OsrmRoutingStrategy strategy = new OsrmRoutingStrategy(baseUrl, null);

        double[] desdeOrigen = strategy.calcularDistanciasDesdeOrigen(
                -32.889458,
                -68.845839,
                List.of(new double[]{-32.925, -68.844}, new double[]{-32.885, -68.838})
        );
        assertEquals(2, desdeOrigen.length);
        assertEquals(3200.0, desdeOrigen[0], 0.01);
        assertEquals(1450.0, desdeOrigen[1], 0.01);

        double[][] matriz = strategy.calcularMatrizDistancias(
                List.of(new double[]{-32.925, -68.844}, new double[]{-32.885, -68.838})
        );
        assertEquals(2, matriz.length);
        assertTrue(matriz[0][1] > 0);
        assertEquals(1800.0, matriz[0][1], 0.01);
    }
}
