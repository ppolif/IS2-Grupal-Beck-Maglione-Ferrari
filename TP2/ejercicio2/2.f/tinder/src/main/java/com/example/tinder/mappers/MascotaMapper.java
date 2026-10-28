package com.example.tinder.mappers;

import com.example.tinder.dto.MascotaDto;
import com.example.tinder.dto.MascotaRequestDto;
import com.example.tinder.entidades.Mascota;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MascotaMapper {

    @Mapping(source = "usuario.id", target = "usuarioId")
    @Mapping(source = "usuario.nombre", target = "usuarioNombre")
    @Mapping(source = "foto.id", target = "fotoId")
    MascotaDto toDto(Mascota mascota);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "foto", ignore = true)
    @Mapping(target = "alta", ignore = true)
    @Mapping(target = "baja", ignore = true)
    Mascota toEntity(MascotaRequestDto requestDto);

    List<MascotaDto> toDtoList(List<Mascota> mascotas);
}

