package com.DulceMayor.backend.dtos.persona;

import com.DulceMayor.backend.dtos.BaseMapper;
import com.DulceMayor.backend.dtos.ContactoMapper;
import com.DulceMayor.backend.dtos.UsuarioResponse;
import com.DulceMayor.backend.dtos.direccion.DireccionMapper;
import com.DulceMayor.backend.entities.Usuario;
import com.DulceMayor.backend.entities.persona.Empleado;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

/**
 * Mapper MapStruct para la entidad {@link Empleado} y sus subclases.
 */
@Mapper(
        componentModel = "spring",
        uses = {DireccionMapper.class, ContactoMapper.class},
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface EmpleadoMapper extends BaseMapper<Empleado, EmpleadoRequest, EmpleadoResponse> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "direcciones", ignore = true)
    @Mapping(target = "contactos", ignore = true)
    Empleado aEntidad(EmpleadoRequest request);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "direcciones", ignore = true)
    @Mapping(target = "contactos", ignore = true)
    void actualizarEntidad(EmpleadoRequest request, @MappingTarget Empleado entidad);

    @Override
    @Mapping(target = "usuario", source = "usuario", qualifiedByName = "usuarioToResponse")
    EmpleadoResponse aResponse(Empleado entidad);

    @Named("usuarioToResponse")
    default UsuarioResponse usuarioToResponse(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombreUsuario(),
                usuario.getRol(),
                usuario.isClaveTemporal(),
                usuario.getProveedorAuth()
        );
    }

    default EmpleadoResumen aResumen(Empleado empleado) {
        if (empleado == null) {
            return null;
        }
        return new EmpleadoResumen(
                empleado.getId(),
                empleado.getNombre(),
                empleado.getApellido(),
                empleado.getUsuario() != null ? empleado.getUsuario().getRol() : null
        );
    }
}

