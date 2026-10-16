package com.example.persona.controllers;

import com.example.persona.dtos.PersonaDto;
import com.example.persona.services.PersonaServiceImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/persona")
public class PersonaController extends BaseControllerImpl<PersonaDto, PersonaServiceImpl> {

    @Override
    protected void validateDtoForSave(PersonaDto dto) throws Exception {
        super.validateDtoForSave(dto);
        if (dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new Exception("El campo 'nombre' es obligatorio.");
        }
    }

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
