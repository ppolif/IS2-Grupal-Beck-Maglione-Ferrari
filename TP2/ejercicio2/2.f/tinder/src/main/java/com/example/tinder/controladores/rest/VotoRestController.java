package com.example.tinder.controladores.rest;

import com.example.tinder.dto.VotoDto;
import com.example.tinder.dto.VotoRequestDto;
import com.example.tinder.dto.VotoRespuestaDto;
import com.example.tinder.entidades.Voto;
import com.example.tinder.errores.ErrorServicio;
import com.example.tinder.mappers.VotoMapper;
import com.example.tinder.servicios.VotoServicio;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/votos")
@CrossOrigin(origins = "*")
public class VotoRestController {

    @Autowired
    private VotoServicio votoServicio;

    @Autowired
    private VotoMapper votoMapper;

    @GetMapping
    public ResponseEntity<List<VotoDto>> listarTodos() {
        List<Voto> votos = votoServicio.listarTodos();
        return ResponseEntity.ok(votoMapper.toDtoList(votos));
    }

    @GetMapping("/recibidos/{idMascota}")
    public ResponseEntity<List<VotoDto>> listarRecibidos(@PathVariable String idMascota) {
        List<Voto> votos = votoServicio.buscarVotosRecibidos(idMascota);
        return ResponseEntity.ok(votoMapper.toDtoList(votos));
    }

    @GetMapping("/propios/{idMascota}")
    public ResponseEntity<List<VotoDto>> listarPropios(@PathVariable String idMascota) {
        List<Voto> votos = votoServicio.buscarVotosPropios(idMascota);
        return ResponseEntity.ok(votoMapper.toDtoList(votos));
    }

    @PostMapping
    public ResponseEntity<VotoDto> votar(@Valid @RequestBody VotoRequestDto request) throws ErrorServicio {
        Voto voto = votoServicio.votar(
                request.getIdUsuario(),
                request.getIdMascota1(),
                request.getIdMascota2()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(votoMapper.toDto(voto));
    }

    @PostMapping("/responder")
    public ResponseEntity<VotoDto> responder(@Valid @RequestBody VotoRespuestaDto request) throws ErrorServicio {
        Voto voto = votoServicio.responder(
                request.getIdUsuario(),
                request.getIdVoto()
        );
        return ResponseEntity.ok(votoMapper.toDto(voto));
    }
}

