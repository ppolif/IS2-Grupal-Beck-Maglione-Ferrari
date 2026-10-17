package com.example.persona.mappers;

import com.example.persona.dtos.LocalidadDto;
import com.example.persona.entities.Localidad;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LocalidadMapper extends BaseMapper<Localidad, LocalidadDto> {
}

