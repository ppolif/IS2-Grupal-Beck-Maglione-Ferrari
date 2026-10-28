package com.example.persona.mappers;

import com.example.persona.dtos.DomicilioDto;
import com.example.persona.entities.Domicilio;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {LocalidadMapper.class})
public interface DomicilioMapper extends BaseMapper<Domicilio, DomicilioDto> {
}

