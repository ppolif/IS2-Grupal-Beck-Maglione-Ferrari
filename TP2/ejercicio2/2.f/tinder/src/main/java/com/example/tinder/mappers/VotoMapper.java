package com.example.tinder.mappers;

import com.example.tinder.dto.VotoDto;
import com.example.tinder.entidades.Voto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VotoMapper {

    @Mapping(source = "mascota1.id", target = "mascota1Id")
    @Mapping(source = "mascota1.nombre", target = "mascota1Nombre")
    @Mapping(source = "mascota2.id", target = "mascota2Id")
    @Mapping(source = "mascota2.nombre", target = "mascota2Nombre")
    VotoDto toDto(Voto voto);

    List<VotoDto> toDtoList(List<Voto> votos);
}

