package com.example.consultorio.controller;

import com.example.consultorio.entities.FotoPaciente;
import com.example.consultorio.service.FotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/foto")
public class FotoController {

    @Autowired
    private FotoService fotoService;

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<byte[]> obtenerFotoPaciente(@PathVariable String pacienteId) {
        Optional<FotoPaciente> fotoOpt = fotoService.obtenerFotoActivaDePaciente(pacienteId);
        if (fotoOpt.isPresent() && fotoOpt.get().getContenido() != null) {
            FotoPaciente foto = fotoOpt.get();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(foto.getMime() != null ? foto.getMime() : "image/jpeg"));
            return new ResponseEntity<>(foto.getContenido(), headers, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
