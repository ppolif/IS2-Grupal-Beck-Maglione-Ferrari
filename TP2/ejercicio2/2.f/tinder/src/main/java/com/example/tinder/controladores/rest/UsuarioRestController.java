package com.example.tinder.controladores.rest;

import com.example.tinder.dto.ApiResponseDto;
import com.example.tinder.dto.UsuarioDto;
import com.example.tinder.dto.UsuarioRegistroDto;
import com.example.tinder.dto.UsuarioUpdateDto;
import com.example.tinder.entidades.Usuario;
import com.example.tinder.errores.ErrorServicio;
import com.example.tinder.mappers.UsuarioMapper;
import com.example.tinder.servicios.UsuarioServicio;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioRestController {

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private UsuarioMapper usuarioMapper;

    @GetMapping
    public ResponseEntity<List<UsuarioDto>> listarTodos() {
        List<Usuario> usuarios = usuarioServicio.buscarTodos();
        return ResponseEntity.ok(usuarioMapper.toDtoList(usuarios));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDto> obtenerPorId(@PathVariable String id) throws ErrorServicio {
        Usuario usuario = usuarioServicio.buscarPorId(id);
        return ResponseEntity.ok(usuarioMapper.toDto(usuario));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UsuarioDto> registrar(@Valid @RequestBody UsuarioRegistroDto request) throws ErrorServicio {
        Usuario usuario = usuarioServicio.registrar(
                null,
                request.getNombre(),
                request.getApellido(),
                request.getEmail(),
                request.getClave(),
                request.getRepetirClave(),
                request.getIdZona()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioMapper.toDto(usuario));
    }

    @PostMapping(value = "/con-foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UsuarioDto> registrarConFoto(
            @RequestParam("nombre") String nombre,
            @RequestParam("apellido") String apellido,
            @RequestParam("email") String email,
            @RequestParam("clave") String clave,
            @RequestParam("repetirClave") String repetirClave,
            @RequestParam("idZona") String idZona,
            @RequestPart(value = "archivo", required = false) MultipartFile archivo) throws ErrorServicio {

        Usuario usuario = usuarioServicio.registrar(
                archivo,
                nombre,
                apellido,
                email,
                clave,
                repetirClave,
                idZona
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioMapper.toDto(usuario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDto> actualizar(
            @PathVariable String id,
            @Valid @RequestBody UsuarioUpdateDto request) throws ErrorServicio {

        Usuario usuario = usuarioServicio.modificar(
                null,
                id,
                request.getNombre(),
                request.getApellido(),
                request.getEmail(),
                request.getClave(),
                request.getRepetirClave(),
                request.getIdZona()
        );
        return ResponseEntity.ok(usuarioMapper.toDto(usuario));
    }

    @PostMapping(value = "/{id}/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UsuarioDto> subirFoto(
            @PathVariable String id,
            @RequestParam("archivo") MultipartFile archivo) throws ErrorServicio {

        Usuario usuario = usuarioServicio.actualizarFoto(id, archivo);
        return ResponseEntity.ok(usuarioMapper.toDto(usuario));
    }

    @PatchMapping("/{id}/deshabilitar")
    public ResponseEntity<ApiResponseDto<Void>> deshabilitar(@PathVariable String id) throws ErrorServicio {
        usuarioServicio.deshabilitar(id);
        return ResponseEntity.ok(ApiResponseDto.ok("Usuario deshabilitado exitosamente"));
    }

    @PatchMapping("/{id}/habilitar")
    public ResponseEntity<ApiResponseDto<Void>> habilitar(@PathVariable String id) throws ErrorServicio {
        usuarioServicio.habilitar(id);
        return ResponseEntity.ok(ApiResponseDto.ok("Usuario habilitado exitosamente"));
    }
}

