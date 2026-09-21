package com.example.zero.services.persona;

import com.example.zero.entidades.persona.Persona;
import com.example.zero.entidades.persona.Usuario;
import com.example.zero.enums.RolUsuario;
import com.example.zero.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public void validar(String nombreUsuario, String clave, RolUsuario rol) {
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario o correo no puede estar vacío");
        }
        if (clave == null || clave.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }
        if (clave.length() < 4) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 4 caracteres");
        }
        if (rol == null) {
            throw new IllegalArgumentException("Debe asignar un rol al usuario");
        }
    }

    @Transactional
    public Usuario crearUsuario(String nombreUsuario, String clave, RolUsuario rol, Persona persona) {
        validar(nombreUsuario, clave, rol);
        String usuarioLimpio = nombreUsuario.trim().toLowerCase();

        Optional<Usuario> existente = usuarioRepository.findByNombreUsuarioAndEliminadoFalse(usuarioLimpio);
        if (existente.isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario activo con el correo o nombre: " + usuarioLimpio);
        }

        Usuario usuario = Usuario.builder()
                .nombreUsuario(usuarioLimpio)
                .clave(clave)
                .rol(rol)
                .persona(persona)
                .eliminado(false)
                .build();

        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario autenticar(String nombreUsuario, String clave) {
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
            throw new IllegalArgumentException("El correo o usuario no puede estar vacío");
        }
        if (clave == null || clave.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }

        String usuarioLimpio = nombreUsuario.trim().toLowerCase();
        Usuario usuario = usuarioRepository.findByNombreUsuarioAndEliminadoFalse(usuarioLimpio)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado o cuenta inactiva"));

        if (!usuario.getClave().equals(clave)) {
            throw new IllegalArgumentException("Contraseña incorrecta");
        }

        return usuario;
    }

    @Transactional
    public Usuario modificarUsuario(String id, String nuevaClave, RolUsuario nuevoRol) {
        Usuario usuario = buscarPorId(id);

        if (nuevaClave != null && !nuevaClave.trim().isEmpty()) {
            if (nuevaClave.length() < 4) {
                throw new IllegalArgumentException("La contraseña debe tener al menos 4 caracteres");
            }
            usuario.setClave(nuevaClave);
        }

        if (nuevoRol != null) {
            usuario.setRol(nuevoRol);
        }

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void eliminarUsuario(String id) {
        Usuario usuario = buscarPorId(id);
        usuario.setEliminado(true);
        usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del usuario no puede ser nulo o vacío");
        }
        return usuarioRepository.findActive(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el usuario activo con ID: " + id));
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorNombreUsuario(String nombreUsuario) {
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario no puede ser nulo o vacío");
        }
        return usuarioRepository.findByNombreUsuarioAndEliminadoFalse(nombreUsuario.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el usuario activo: " + nombreUsuario));
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarActivos() {
        return usuarioRepository.findByEliminadoFalse();
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }
}

