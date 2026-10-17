package com.example.persona.mappers;

import com.example.persona.dtos.LibroDto;
import com.example.persona.entities.Libro;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {AutorMapper.class}, builder = @Builder(disableBuilder = true))
public interface LibroMapper extends BaseMapper<Libro, LibroDto> {
}

