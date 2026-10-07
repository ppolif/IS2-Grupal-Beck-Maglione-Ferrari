package com.example.persona.services;

import com.example.persona.dtos.PersonaDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PersonaService extends BaseService<PersonaDto, Long> {

    List<PersonaDto> search(String filtro) throws Exception;
    Page<PersonaDto> search(String filtro, Pageable pageable) throws Exception;
}
