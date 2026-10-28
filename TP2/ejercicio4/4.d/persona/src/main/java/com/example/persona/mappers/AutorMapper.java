package com.example.persona.mappers;

import com.example.persona.dtos.AutorDto;
import com.example.persona.entities.Autor;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AutorMapper extends BaseMapper<Autor, AutorDto> {
}

