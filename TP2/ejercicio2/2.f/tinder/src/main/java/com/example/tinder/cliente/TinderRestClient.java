package com.example.tinder.cliente;

import com.example.tinder.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class TinderRestClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    @Autowired
    public TinderRestClient(
            RestTemplate restTemplate,
            @Value("${server.port:9000}") Integer port) {
        this.restTemplate = restTemplate;
        this.baseUrl = "http://localhost:" + port + "/api";
    }

    // Constructor para pruebas o configuración manual
    public TinderRestClient(RestTemplate restTemplate, String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    // ==================== ZONAS ====================

    public List<ZonaDto> listarZonas() {
        ResponseEntity<List<ZonaDto>> response = restTemplate.exchange(
                baseUrl + "/zonas",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ZonaDto>>() {}
        );
        return response.getBody();
    }

    public ZonaDto obtenerZona(String id) {
        return restTemplate.getForObject(baseUrl + "/zonas/" + id, ZonaDto.class);
    }

    public ZonaDto crearZona(ZonaRequestDto request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ZonaRequestDto> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ZonaDto> response = restTemplate.postForEntity(
                baseUrl + "/zonas",
                entity,
                ZonaDto.class
        );
        return response.getBody();
    }

    // ==================== USUARIOS ====================

    public List<UsuarioDto> listarUsuarios() {
        ResponseEntity<List<UsuarioDto>> response = restTemplate.exchange(
                baseUrl + "/usuarios",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<UsuarioDto>>() {}
        );
        return response.getBody();
    }

    public UsuarioDto obtenerUsuario(String id) {
        return restTemplate.getForObject(baseUrl + "/usuarios/" + id, UsuarioDto.class);
    }

    public UsuarioDto registrarUsuario(UsuarioRegistroDto request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UsuarioRegistroDto> entity = new HttpEntity<>(request, headers);

        ResponseEntity<UsuarioDto> response = restTemplate.postForEntity(
                baseUrl + "/usuarios",
                entity,
                UsuarioDto.class
        );
        return response.getBody();
    }

    public UsuarioDto actualizarUsuario(String id, UsuarioUpdateDto request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UsuarioUpdateDto> entity = new HttpEntity<>(request, headers);

        ResponseEntity<UsuarioDto> response = restTemplate.exchange(
                baseUrl + "/usuarios/" + id,
                HttpMethod.PUT,
                entity,
                UsuarioDto.class
        );
        return response.getBody();
    }

    // ==================== MASCOTAS ====================

    public List<MascotaDto> listarMascotas() {
        ResponseEntity<List<MascotaDto>> response = restTemplate.exchange(
                baseUrl + "/mascotas",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<MascotaDto>>() {}
        );
        return response.getBody();
    }

    public MascotaDto obtenerMascota(String id) {
        return restTemplate.getForObject(baseUrl + "/mascotas/" + id, MascotaDto.class);
    }

    public List<MascotaDto> listarMascotasPorUsuario(String usuarioId) {
        ResponseEntity<List<MascotaDto>> response = restTemplate.exchange(
                baseUrl + "/mascotas/usuario/" + usuarioId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<MascotaDto>>() {}
        );
        return response.getBody();
    }

    public MascotaDto crearMascota(MascotaRequestDto request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MascotaRequestDto> entity = new HttpEntity<>(request, headers);

        ResponseEntity<MascotaDto> response = restTemplate.postForEntity(
                baseUrl + "/mascotas",
                entity,
                MascotaDto.class
        );
        return response.getBody();
    }

    public MascotaDto actualizarMascota(String id, MascotaRequestDto request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MascotaRequestDto> entity = new HttpEntity<>(request, headers);

        ResponseEntity<MascotaDto> response = restTemplate.exchange(
                baseUrl + "/mascotas/" + id,
                HttpMethod.PUT,
                entity,
                MascotaDto.class
        );
        return response.getBody();
    }

    public void eliminarMascota(String id, String idUsuario) {
        String url = baseUrl + "/mascotas/" + id + (idUsuario != null ? "?idUsuario=" + idUsuario : "");
        restTemplate.delete(url);
    }

    // ==================== VOTOS ====================

    public List<VotoDto> listarVotos() {
        ResponseEntity<List<VotoDto>> response = restTemplate.exchange(
                baseUrl + "/votos",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<VotoDto>>() {}
        );
        return response.getBody();
    }

    public VotoDto votar(VotoRequestDto request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<VotoRequestDto> entity = new HttpEntity<>(request, headers);

        ResponseEntity<VotoDto> response = restTemplate.postForEntity(
                baseUrl + "/votos",
                entity,
                VotoDto.class
        );
        return response.getBody();
    }

    public VotoDto responderVoto(VotoRespuestaDto request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<VotoRespuestaDto> entity = new HttpEntity<>(request, headers);

        ResponseEntity<VotoDto> response = restTemplate.postForEntity(
                baseUrl + "/votos/responder",
                entity,
                VotoDto.class
        );
        return response.getBody();
    }
}
