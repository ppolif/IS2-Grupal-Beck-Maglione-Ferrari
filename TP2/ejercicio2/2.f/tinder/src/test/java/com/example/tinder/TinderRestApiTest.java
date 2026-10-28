package com.example.tinder;

import com.example.tinder.cliente.TinderRestClient;
import com.example.tinder.dto.*;
import com.example.tinder.enumeraciones.Sexo;
import com.example.tinder.enumeraciones.Tipo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TinderRestApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private RestTemplate springRestTemplate;

    private RestTemplate restTemplate;
    private TinderRestClient client;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        this.baseUrl = "http://localhost:" + port + "/api";
        this.restTemplate = new RestTemplate();
        this.client = new TinderRestClient(springRestTemplate, baseUrl);
    }

    @Test
    void testListarZonasViaRest() {
        ResponseEntity<List<ZonaDto>> response = restTemplate.exchange(
                baseUrl + "/zonas",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ZonaDto>>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty(), "Debe haber zonas inicializadas");
    }

    @Test
    void testCrearZonaViaRest() {
        ZonaRequestDto req = new ZonaRequestDto("Zona Test", "Descripción test");
        ResponseEntity<ZonaDto> response = restTemplate.postForEntity(
                baseUrl + "/zonas",
                req,
                ZonaDto.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Zona Test", response.getBody().getNombre());
        assertNotNull(response.getBody().getId());
    }

    @Test
    void testCicloCompletoConRestTemplateYMapStruct() {
        // 1. Obtener una zona existente con RestTemplate
        List<ZonaDto> zonas = client.listarZonas();
        assertNotNull(zonas);
        assertFalse(zonas.isEmpty());
        String idZona = zonas.get(0).getId();

        // 2. Registrar dos usuarios vía RestTemplate
        long timestamp = System.currentTimeMillis();
        UsuarioRegistroDto u1Req = new UsuarioRegistroDto(
                "Lucas",
                "Gomez",
                "lucas" + timestamp + "@mail.com",
                "secreta123",
                "secreta123",
                idZona
        );
        UsuarioDto user1 = client.registrarUsuario(u1Req);
        assertNotNull(user1);
        assertNotNull(user1.getId());
        assertEquals("Lucas", user1.getNombre());

        UsuarioRegistroDto u2Req = new UsuarioRegistroDto(
                "Maria",
                "Lopez",
                "maria" + timestamp + "@mail.com",
                "secreta123",
                "secreta123",
                idZona
        );
        UsuarioDto user2 = client.registrarUsuario(u2Req);
        assertNotNull(user2);
        assertNotNull(user2.getId());

        // 3. Crear mascotas para cada usuario vía RestTemplate
        MascotaRequestDto m1Req = new MascotaRequestDto(
                "Rocky",
                Sexo.MACHO,
                Tipo.PERRO,
                user1.getId()
        );
        MascotaDto mascota1 = client.crearMascota(m1Req);
        assertNotNull(mascota1);
        assertNotNull(mascota1.getId());
        assertEquals("Rocky", mascota1.getNombre());
        assertEquals(user1.getId(), mascota1.getUsuarioId());

        MascotaRequestDto m2Req = new MascotaRequestDto(
                "Mimi",
                Sexo.HEMBRA,
                Tipo.GATO,
                user2.getId()
        );
        MascotaDto mascota2 = client.crearMascota(m2Req);
        assertNotNull(mascota2);
        assertNotNull(mascota2.getId());
        assertEquals("Mimi", mascota2.getNombre());

        // 4. Emitir un voto entre mascotas vía RestTemplate
        VotoRequestDto votoReq = new VotoRequestDto(
                user1.getId(),
                mascota1.getId(),
                mascota2.getId()
        );
        VotoDto voto = client.votar(votoReq);
        assertNotNull(voto);
        assertNotNull(voto.getId());
        assertEquals(mascota1.getId(), voto.getMascota1Id());
        assertEquals(mascota2.getId(), voto.getMascota2Id());

        // 5. Responder el voto vía RestTemplate
        VotoRespuestaDto respReq = new VotoRespuestaDto(
                user2.getId(),
                voto.getId()
        );
        VotoDto votoRespondido = client.responderVoto(respReq);
        assertNotNull(votoRespondido);
        assertNotNull(votoRespondido.getRespuesta(), "La respuesta del voto no debe ser nula");
    }
}
