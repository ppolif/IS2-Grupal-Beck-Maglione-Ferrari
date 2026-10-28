package com.example.tinder.mappers;

import com.example.tinder.dto.ZonaDto;
import com.example.tinder.dto.ZonaRequestDto;
import com.example.tinder.entidades.Zona;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ZonaMapper {

    ZonaDto toDto(Zona zona);

    @Mapping(target = "id", ignore = true)
    Zona toEntity(ZonaRequestDto requestDto);

    Zona toEntity(ZonaDto dto);

    List<ZonaDto> toDtoList(List<Zona> zonas);
}

