package com.example.tinder.controladores.rest;

import com.example.tinder.dto.FotoDto;
import com.example.tinder.entidades.Foto;
import com.example.tinder.entidades.Mascota;
import com.example.tinder.entidades.Usuario;
import com.example.tinder.errores.ErrorServicio;
import com.example.tinder.mappers.FotoMapper;
import com.example.tinder.repositorios.FotoRepositorio;
import com.example.tinder.servicios.FotoServicio;
import com.example.tinder.servicios.MascotaServicio;
import com.example.tinder.servicios.UsuarioServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController
@RequestMapping("/api/fotos")
@CrossOrigin(origins = "*")
public class FotoRestController {

    @Autowired
    private FotoServicio fotoServicio;

    @Autowired
    private FotoRepositorio fotoRepositorio;

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private MascotaServicio mascotaServicio;

    @Autowired
    private FotoMapper fotoMapper;

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> obtenerPorId(@PathVariable String id) {
        Optional<Foto> opt = fotoRepositorio.findById(id);
        if (opt.isPresent()) {
            Foto foto = opt.get();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(foto.getMime() != null ? foto.getMime() : MediaType.IMAGE_JPEG_VALUE));
            return new ResponseEntity<>(foto.getContenido(), headers, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<byte[]> fotoUsuario(@PathVariable String idUsuario) throws ErrorServicio {
        Usuario usuario = usuarioServicio.buscarPorId(idUsuario);
        if (usuario.getFoto() == null || usuario.getFoto().getContenido() == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        HttpHeaders headers = new HttpHeaders();
        String mime = usuario.getFoto().getMime() != null ? usuario.getFoto().getMime() : MediaType.IMAGE_JPEG_VALUE;
        headers.setContentType(MediaType.parseMediaType(mime));
        return new ResponseEntity<>(usuario.getFoto().getContenido(), headers, HttpStatus.OK);
    }

    @GetMapping("/mascota/{idMascota}")
    public ResponseEntity<byte[]> fotoMascota(@PathVariable String idMascota) throws ErrorServicio {
        Mascota mascota = mascotaServicio.buscarPorId(idMascota);
        if (mascota.getFoto() == null || mascota.getFoto().getContenido() == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        HttpHeaders headers = new HttpHeaders();
        String mime = mascota.getFoto().getMime() != null ? mascota.getFoto().getMime() : MediaType.IMAGE_JPEG_VALUE;
        headers.setContentType(MediaType.parseMediaType(mime));
        return new ResponseEntity<>(mascota.getFoto().getContenido(), headers, HttpStatus.OK);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FotoDto> subirFoto(@RequestParam("archivo") MultipartFile archivo) throws ErrorServicio {
        Foto foto = fotoServicio.guardar(archivo);
        if (foto != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(fotoMapper.toDto(foto));
        }
        return ResponseEntity.badRequest().build();
    }
}

