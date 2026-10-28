package com.example.persona.mappers;

import com.example.persona.dtos.LibroFisicoDto;
import com.example.persona.entities.LibroFisico;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {AutorMapper.class}, builder = @Builder(disableBuilder = true))
public interface LibroFisicoMapper extends BaseMapper<LibroFisico, LibroFisicoDto> {
}

