package org.example.servicios;


import org.example.dtos.UsuarioRequestDTO;
import org.example.dtos.UsuarioResponseDTO;
import org.example.entidades.Usuario;
import org.example.entidades.enumeraciones.Rol;
import org.example.errores.RecursoNoEncontradoException;
import org.example.repositorios.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioServicioImpl implements UserDetailsService { // Acá también podés implementar tu interfaz UsuarioServicio

    @Autowired
    private final UsuarioRepositorio usuarioRepositorio;

    private final PasswordEncoder passwordEncoder;

    // Usamos @Lazy en el encoder para evitar dependencias circulares con la configuración de seguridad
    public UsuarioServicioImpl(UsuarioRepositorio usuarioRepositorio, @Lazy PasswordEncoder passwordEncoder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponseDTO registrarUsuario(UsuarioRequestDTO dto) {
        if (usuarioRepositorio.existsByMail(dto.getMail())) {
            throw new IllegalArgumentException("Ya existe un usuario con este mail");
        }
        if (usuarioRepositorio.existsByDni(dto.getDni())) {
            throw new IllegalArgumentException("Ya existe un usuario con este DNI");
        }

        Usuario usuario = Usuario.builder()
                .dni(dto.getDni())
                .nombre(dto.getNombre())
                .mail(dto.getMail())
                .clave(passwordEncoder.encode(dto.getClave())) // Encriptación BCrypt
                .rol(Rol.USUARIO) // Rol por defecto para registros públicos
                .alta(true)
                .build();

        usuario = usuarioRepositorio.save(usuario);
        return mapearADTO(usuario);
    }

    // Método obligatorio de Spring Security para buscar el usuario al momento de loguearse
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String mail) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepositorio.findByMailAndAltaTrue(mail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado o dado de baja"));

        // Spring Security requiere que los roles tengan el prefijo "ROLE_"
        List<GrantedAuthority> permisos = new ArrayList<>();
        permisos.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));

        // Devolvemos el User propio de Security con los datos extraídos de nuestro Usuario
        return new org.springframework.security.core.userdetails.User(
                usuario.getMail(),
                usuario.getClave(),
                permisos
        );
    }

    private UsuarioResponseDTO mapearADTO(Usuario usuario) {
        return UsuarioResponseDTO.builder()
                .id(usuario.getId())
                .dni(usuario.getDni())
                .nombre(usuario.getNombre())
                .mail(usuario.getMail())
                .rol(usuario.getRol())
                .alta(usuario.isAlta())
                .build();
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> obtenerTodosActivos() {
        return usuarioRepositorio.findByAltaTrue().stream()
                .map(this::mapearADTO)
                .collect(   Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerPorId(String id) {
        Usuario usuario = usuarioRepositorio.findByIdAndAltaTrue(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado o inactivo"));
        return mapearADTO(usuario);
    }

    @Transactional
    public UsuarioResponseDTO actualizar(String id, UsuarioRequestDTO dto) {
        Usuario usuario = usuarioRepositorio.findByIdAndAltaTrue(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado o inactivo"));

        // Verificamos que si cambió el mail o el DNI, no colisione con otro usuario existente
        if (!usuario.getMail().equals(dto.getMail()) && usuarioRepositorio.existsByMail(dto.getMail())) {
            throw new IllegalArgumentException("Ya existe otro usuario con este mail");
        }
        if (usuario.getDni() != dto.getDni() && usuarioRepositorio.existsByDni(dto.getDni())) {
            throw new IllegalArgumentException("Ya existe otro usuario con este DNI");
        }

        usuario.setDni(dto.getDni());
        usuario.setNombre(dto.getNombre());
        usuario.setMail(dto.getMail());
        usuario.setClave(passwordEncoder.encode(dto.getClave()));

        usuario = usuarioRepositorio.save(usuario);
        return mapearADTO(usuario);
    }

    @Transactional
    public void darDeBaja(String id) {
        Usuario usuario = usuarioRepositorio.findByIdAndAltaTrue(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado o inactivo"));
        usuario.setAlta(false);
        usuarioRepositorio.save(usuario);
    }
}
