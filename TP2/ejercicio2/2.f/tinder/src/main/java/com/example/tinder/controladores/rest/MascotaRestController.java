package com.example.tinder.controladores.rest;

import com.example.tinder.dto.ApiResponseDto;
import com.example.tinder.dto.MascotaDto;
import com.example.tinder.dto.MascotaRequestDto;
import com.example.tinder.entidades.Mascota;
import com.example.tinder.enumeraciones.Sexo;
import com.example.tinder.enumeraciones.Tipo;
import com.example.tinder.errores.ErrorServicio;
import com.example.tinder.mappers.MascotaMapper;
import com.example.tinder.servicios.MascotaServicio;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/mascotas")
@CrossOrigin(origins = "*")
public class MascotaRestController {

    @Autowired
    private MascotaServicio mascotaServicio;

    @Autowired
    private MascotaMapper mascotaMapper;

    @GetMapping
    public ResponseEntity<List<MascotaDto>> listarTodas() {
        List<Mascota> mascotas = mascotaServicio.listarTodas();
        return ResponseEntity.ok(mascotaMapper.toDtoList(mascotas));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MascotaDto> obtenerPorId(@PathVariable String id) throws ErrorServicio {
        Mascota mascota = mascotaServicio.buscarPorId(id);
        return ResponseEntity.ok(mascotaMapper.toDto(mascota));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<MascotaDto>> listarPorUsuario(@PathVariable String usuarioId) {
        List<Mascota> mascotas = mascotaServicio.buscarMascotasPorUsuario(usuarioId);
        return ResponseEntity.ok(mascotaMapper.toDtoList(mascotas));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MascotaDto> crear(@Valid @RequestBody MascotaRequestDto request) throws ErrorServicio {
        Mascota mascota = mascotaServicio.agregarMascota(
                null,
                request.getIdUsuario(),
                request.getNombre(),
                request.getSexo(),
                request.getTipo()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(mascotaMapper.toDto(mascota));
    }

    @PostMapping(value = "/con-foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MascotaDto> crearConFoto(
            @RequestParam("idUsuario") String idUsuario,
            @RequestParam("nombre") String nombre,
            @RequestParam("sexo") Sexo sexo,
            @RequestParam("tipo") Tipo tipo,
            @RequestPart(value = "archivo", required = false) MultipartFile archivo) throws ErrorServicio {

        Mascota mascota = mascotaServicio.agregarMascota(archivo, idUsuario, nombre, sexo, tipo);
        return ResponseEntity.status(HttpStatus.CREATED).body(mascotaMapper.toDto(mascota));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MascotaDto> actualizar(
            @PathVariable String id,
            @Valid @RequestBody MascotaRequestDto request) throws ErrorServicio {

        Mascota mascota = mascotaServicio.actualizar(
                null,
                request.getIdUsuario(),
                id,
                request.getNombre(),
                request.getSexo(),
                request.getTipo()
        );
        return ResponseEntity.ok(mascotaMapper.toDto(mascota));
    }

    @PostMapping(value = "/{id}/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MascotaDto> subirFoto(
            @PathVariable String id,
            @RequestParam("archivo") MultipartFile archivo) throws ErrorServicio {

        Mascota mascota = mascotaServicio.actualizarFoto(id, archivo);
        return ResponseEntity.ok(mascotaMapper.toDto(mascota));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> eliminar(
            @PathVariable String id,
            @RequestParam(required = false) String idUsuario) throws ErrorServicio {

        // Si idUsuario no es provisto, obtenemos el usuario dueño de la mascota
        if (idUsuario == null || idUsuario.isEmpty()) {
            Mascota m = mascotaServicio.buscarPorId(id);
            idUsuario = m.getUsuario().getId();
        }

        mascotaServicio.eliminarMascota(idUsuario, id);
        return ResponseEntity.ok(ApiResponseDto.ok("Mascota dada de baja exitosamente"));
    }
}

