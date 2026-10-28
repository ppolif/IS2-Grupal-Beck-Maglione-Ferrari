package com.example.tinder.mappers;

import com.example.tinder.dto.UsuarioDto;
import com.example.tinder.dto.UsuarioRegistroDto;
import com.example.tinder.entidades.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ZonaMapper.class})
public interface UsuarioMapper {

    @Mapping(source = "foto.id", target = "fotoId")
    @Mapping(source = "rol", target = "rol", qualifiedByName = "rolToString")
    UsuarioDto toDto(Usuario usuario);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "zona", ignore = true)
    @Mapping(target = "foto", ignore = true)
    @Mapping(target = "alta", ignore = true)
    @Mapping(target = "baja", ignore = true)
    @Mapping(target = "rol", ignore = true)
    Usuario toEntity(UsuarioRegistroDto registroDto);

    List<UsuarioDto> toDtoList(List<Usuario> usuarios);

    @Named("rolToString")
    default String rolToString(com.example.tinder.enumeraciones.Rol rol) {
        return rol != null ? rol.name() : null;
    }
}

