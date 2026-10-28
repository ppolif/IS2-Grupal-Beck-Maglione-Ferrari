package com.example.persona.services;

import com.example.persona.dtos.PersonaDto;
import com.example.persona.entities.Persona;
import com.example.persona.repositories.BaseRepository;
import com.example.persona.repositories.PersonaRepository;
import com.example.persona.mappers.PersonaMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PersonaServiceImpl extends BaseServiceImpl<Persona, PersonaDto, Long> implements PersonaService {

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    public PersonaServiceImpl(BaseRepository<Persona, Long> baseRepository, PersonaMapper personaMapper) {
        super(baseRepository, personaMapper);
    }

    // ============================================================
    // HOOKS ESPECIALIZADOS DEL PATRÓN TEMPLATE METHOD
    // ============================================================

    @Override
    protected void validateDto(PersonaDto dto) throws Exception {
        super.validateDto(dto);
        if (dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new Exception("El nombre de la persona no puede estar vacío.");
        }
        if (dto.getApellido() == null || dto.getApellido().isBlank()) {
            throw new Exception("El apellido de la persona no puede estar vacío.");
        }
        if (dto.getDni() <= 0) {
            throw new Exception("El DNI de la persona debe ser un número positivo.");
        }
    }

    @Override
    protected void beforeSave(Persona persona, PersonaDto dto) throws Exception {
        super.beforeSave(persona, dto);
        if (persona.getNombre() != null) {
            persona.setNombre(persona.getNombre().trim());
        }
        if (persona.getApellido() != null) {
            persona.setApellido(persona.getApellido().trim());
        }
        if (persona.getEmail() != null) {
            persona.setEmail(persona.getEmail().trim().toLowerCase());
        }
    }

    @Override
    protected void beforeDelete(Persona persona) throws Exception {
        super.beforeDelete(persona);
        if (persona.getLibros() != null && !persona.getLibros().isEmpty()) {
            throw new Exception("No se puede eliminar la persona porque posee libros asociados.");
        }
    }

    // ============================================================
    // BÚSQUEDAS ESPECÍFICAS
    // ============================================================

    @Override
    public List<PersonaDto> search(String filtro) throws Exception {
        try {
            List<Persona> personas = personaRepository.searchNativo(filtro);
            return personas.stream().map(this::toDto).toList();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Page<PersonaDto> search(String filtro, Pageable pageable) throws Exception {
        try {
            Page<Persona> personas = personaRepository.searchNativo(filtro, pageable);
            return personas.map(this::toDto);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}
