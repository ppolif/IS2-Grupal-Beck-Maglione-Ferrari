package com.example.tinder.controladores.rest;

import com.example.tinder.dto.ZonaDto;
import com.example.tinder.dto.ZonaRequestDto;
import com.example.tinder.entidades.Zona;
import com.example.tinder.errores.ErrorServicio;
import com.example.tinder.mappers.ZonaMapper;
import com.example.tinder.servicios.ZonaServicio;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/zonas")
@CrossOrigin(origins = "*")
public class ZonaRestController {

    @Autowired
    private ZonaServicio zonaServicio;

    @Autowired
    private ZonaMapper zonaMapper;

    @GetMapping
    public ResponseEntity<List<ZonaDto>> listarTodas() {
        List<Zona> zonas = zonaServicio.listarTodas();
        return ResponseEntity.ok(zonaMapper.toDtoList(zonas));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ZonaDto> obtenerPorId(@PathVariable String id) throws ErrorServicio {
        Zona zona = zonaServicio.buscarPorId(id);
        return ResponseEntity.ok(zonaMapper.toDto(zona));
    }

    @PostMapping
    public ResponseEntity<ZonaDto> crear(@Valid @RequestBody ZonaRequestDto request) throws ErrorServicio {
        Zona zona = zonaServicio.crearZona(request.getNombre(), request.getDescripcion());
        return ResponseEntity.status(HttpStatus.CREATED).body(zonaMapper.toDto(zona));
    }
}

