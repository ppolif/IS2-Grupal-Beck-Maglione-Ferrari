package com.example.persona.mappers;

import com.example.persona.dtos.PersonaDto;
import com.example.persona.entities.Persona;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {DomicilioMapper.class, LibroMapper.class})
public interface PersonaMapper extends BaseMapper<Persona, PersonaDto> {
}

