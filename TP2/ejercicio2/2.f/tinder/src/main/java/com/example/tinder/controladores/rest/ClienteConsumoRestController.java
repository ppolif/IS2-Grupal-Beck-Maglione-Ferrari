package com.example.tinder.controladores.rest;

import com.example.tinder.cliente.TinderRestClient;
import com.example.tinder.dto.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador que demuestra el consumo de la API REST de Tinder de Mascota
 * utilizando RestTemplate y mapeando las respuestas con DTOs.
 */
@RestController
@RequestMapping("/api/consumo")
@CrossOrigin(origins = "*")
public class ClienteConsumoRestController {

    @Autowired
    private TinderRestClient tinderRestClient;

    @GetMapping("/zonas")
    public ResponseEntity<List<ZonaDto>> consumirListadoZonas() {
        return ResponseEntity.ok(tinderRestClient.listarZonas());
    }

    @PostMapping("/zonas")
    public ResponseEntity<ZonaDto> consumirCrearZona(@Valid @RequestBody ZonaRequestDto request) {
        return ResponseEntity.ok(tinderRestClient.crearZona(request));
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioDto>> consumirListadoUsuarios() {
        return ResponseEntity.ok(tinderRestClient.listarUsuarios());
    }

    @GetMapping("/usuarios/{id}")
    public ResponseEntity<UsuarioDto> consumirObtenerUsuario(@PathVariable String id) {
        return ResponseEntity.ok(tinderRestClient.obtenerUsuario(id));
    }

    @PostMapping("/usuarios")
    public ResponseEntity<UsuarioDto> consumirRegistrarUsuario(@Valid @RequestBody UsuarioRegistroDto request) {
        return ResponseEntity.ok(tinderRestClient.registrarUsuario(request));
    }

    @GetMapping("/mascotas")
    public ResponseEntity<List<MascotaDto>> consumirListadoMascotas() {
        return ResponseEntity.ok(tinderRestClient.listarMascotas());
    }

    @GetMapping("/mascotas/{id}")
    public ResponseEntity<MascotaDto> consumirObtenerMascota(@PathVariable String id) {
        return ResponseEntity.ok(tinderRestClient.obtenerMascota(id));
    }

    @PostMapping("/mascotas")
    public ResponseEntity<MascotaDto> consumirCrearMascota(@Valid @RequestBody MascotaRequestDto request) {
        return ResponseEntity.ok(tinderRestClient.crearMascota(request));
    }

    @GetMapping("/votos")
    public ResponseEntity<List<VotoDto>> consumirListadoVotos() {
        return ResponseEntity.ok(tinderRestClient.listarVotos());
    }

    @PostMapping("/votar")
    public ResponseEntity<VotoDto> consumirVotar(@Valid @RequestBody VotoRequestDto request) {
        return ResponseEntity.ok(tinderRestClient.votar(request));
    }

    @PostMapping("/responder-voto")
    public ResponseEntity<VotoDto> consumirResponderVoto(@Valid @RequestBody VotoRespuestaDto request) {
        return ResponseEntity.ok(tinderRestClient.responderVoto(request));
    }
}

