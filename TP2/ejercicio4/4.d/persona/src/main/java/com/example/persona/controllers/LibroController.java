package com.example.persona.controllers;

import com.example.persona.dtos.LibroDto;
import com.example.persona.services.LibroServiceImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/libros")
public class LibroController extends BaseControllerImpl<LibroDto, LibroServiceImpl> {

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String filtro) {
        return executeTemplate(
                HttpStatus.OK,
                HttpStatus.NOT_FOUND,
                () -> servicio.search(filtro),
                "search"
        );
    }

    @GetMapping("/searchPaged")
    public ResponseEntity<?> search(@RequestParam String filtro, Pageable pageable) {
        return executeTemplate(
                HttpStatus.OK,
                HttpStatus.NOT_FOUND,
                () -> servicio.search(filtro, pageable),
                "searchPaged"
        );
    }
}
