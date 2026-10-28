package com.example.tinder.mappers;

import com.example.tinder.dto.FotoDto;
import com.example.tinder.entidades.Foto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FotoMapper {

    FotoDto toDto(Foto foto);

    @Mapping(target = "contenido", ignore = true)
    Foto toEntity(FotoDto dto);
}
