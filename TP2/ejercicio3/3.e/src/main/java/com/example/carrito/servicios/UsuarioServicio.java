package com.example.carrito.servicios;

import com.example.carrito.dtos.UsuarioRegistroDTO;
import com.example.carrito.entidades.Usuario;
import com.example.carrito.entidades.enumeraciones.Rol;
import com.example.carrito.excepciones.ErrorServicio;
import com.example.carrito.repositorios.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioServicio {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public Usuario registrar(UsuarioRegistroDTO dto) throws ErrorServicio {
        validarRegistro(dto);

        Usuario usuario = Usuario.builder()
                .nombre(dto.getNombre().trim())
                .email(dto.getEmail().trim().toLowerCase())
                .clave(passwordEncoder.encode(dto.getClave()))
                .rol(Rol.CLIENTE)
                .eliminado(false)
                .build();

        return usuarioRepositorio.save(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorEmail(String email) throws ErrorServicio {
        return usuarioRepositorio.findByEmailAndEliminadoFalse(email.trim().toLowerCase())
                .orElseThrow(() -> new ErrorServicio("Usuario no encontrado con el email: " + email));
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(String id) throws ErrorServicio {
        return usuarioRepositorio.findById(id)
                .filter(u -> !u.isEliminado())
                .orElseThrow(() -> new ErrorServicio("Usuario no encontrado con ID: " + id));
    }

    private void validarRegistro(UsuarioRegistroDTO dto) throws ErrorServicio {
        if (dto == null) {
            throw new ErrorServicio("Los datos de registro no pueden ser nulos");
        }
        if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
            throw new ErrorServicio("El nombre no puede estar vacío");
        }
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            throw new ErrorServicio("El email no puede estar vacío");
        }
        if (dto.getClave() == null || dto.getClave().trim().isEmpty()) {
            throw new ErrorServicio("La clave no puede estar vacía");
        }
        if (dto.getConfirmacionClave() == null || dto.getConfirmacionClave().trim().isEmpty()) {
            throw new ErrorServicio("La confirmación de la clave no puede estar vacía");
        }
        if (!dto.getClave().equals(dto.getConfirmacionClave())) {
            throw new ErrorServicio("La clave y su confirmación no coinciden");
        }
        if (usuarioRepositorio.existsByEmailAndEliminadoFalse(dto.getEmail().trim().toLowerCase())) {
            throw new ErrorServicio("Ya existe un usuario registrado con el email: " + dto.getEmail());
        }
    }
}

