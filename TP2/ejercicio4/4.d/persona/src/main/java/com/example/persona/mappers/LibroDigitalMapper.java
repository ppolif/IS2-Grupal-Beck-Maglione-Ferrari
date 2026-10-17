package com.example.persona.mappers;

import com.example.persona.dtos.LibroDigitalDto;
import com.example.persona.entities.LibroDigital;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {AutorMapper.class}, builder = @Builder(disableBuilder = true))
public interface LibroDigitalMapper extends BaseMapper<LibroDigital, LibroDigitalDto> {
}

