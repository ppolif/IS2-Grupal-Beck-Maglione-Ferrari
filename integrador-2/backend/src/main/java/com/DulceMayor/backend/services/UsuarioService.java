package com.DulceMayor.backend.services;

import com.DulceMayor.backend.config.CredencialesInvalidasException;
import com.DulceMayor.backend.config.JwtProvider;
import com.DulceMayor.backend.config.RecursoNoEncontradoException;
import com.DulceMayor.backend.config.UsuarioPrincipal;
import com.DulceMayor.backend.config.ValidacionException;
import com.DulceMayor.backend.dtos.AuthResponse;
import com.DulceMayor.backend.dtos.CambiarClaveRequest;
import com.DulceMayor.backend.dtos.LoginRequest;
import com.DulceMayor.backend.dtos.PerfilResponse;
import com.DulceMayor.backend.dtos.UsuarioResponse;
import com.DulceMayor.backend.entities.ProveedorAuth;
import com.DulceMayor.backend.entities.RolUsuario;
import com.DulceMayor.backend.entities.Usuario;
import com.DulceMayor.backend.repositories.UsuarioRepository;
import java.util.Optional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de negocio para la gestión de autenticación y credenciales de Usuario.
 * Aplica los patrones Experto en Información e Inyección de Dependencias por constructor.
 */
@Service
public class UsuarioService implements UserDetailsService {

    public static final String CLAVE_POR_DEFECTO = Usuario.CLAVE_POR_DEFECTO;
    public static final String ClavePorDefecto = Usuario.CLAVE_POR_DEFECTO;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtProvider jwtProvider) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByNombreUsuarioAndEliminadoFalse(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado o inactivo"));
        return new UsuarioPrincipal(usuario);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarActivoPorId(String id) {
        return usuarioRepository.findByIdAndEliminadoFalse(id);
    }

    @Transactional(readOnly = true)
    public Usuario buscarUsuario(String id) {
        return usuarioRepository.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    @Transactional
    public Usuario crearUsuarioConClavePorDefecto(String nombreUsuario, RolUsuario rol) {
        Usuario usuario = Usuario.builder()
                .nombreUsuario(nombreUsuario)
                .clave(passwordEncoder.encode(CLAVE_POR_DEFECTO))
                .rol(rol)
                .claveTemporal(true)
                .proveedorAuth(ProveedorAuth.LOCAL)
                .eliminado(false)
                .build();
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void crearUsuarioSemillaSiNoExiste(String nombreUsuario, RolUsuario rol) {
        if (!usuarioRepository.existsByNombreUsuario(nombreUsuario)) {
            crearUsuarioConClavePorDefecto(nombreUsuario, rol);
        }
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Usuario usuario = login(request.nombreUsuario(), request.clave());
        String token = jwtProvider.generarToken(usuario);
        return new AuthResponse(
                token,
                "Bearer",
                jwtProvider.getExpiraEnSegundos(),
                usuario.isClaveTemporal(),
                toResponse(usuario)
        );
    }

    @Transactional(readOnly = true)
    public Usuario login(String nombreUsuario, String clave) {
        Optional<Usuario> candidato = usuarioRepository.findByNombreUsuario(nombreUsuario);
        if (candidato.isEmpty()) {
            throw new CredencialesInvalidasException();
        }
        Usuario usuario = candidato.get();
        if (usuario.isEliminado() || !passwordEncoder.matches(clave, usuario.getClave())) {
            throw new CredencialesInvalidasException();
        }
        return usuario;
    }

    @Transactional(readOnly = true)
    public boolean requiereCambioClave(String id) {
        return buscarUsuario(id).isClaveTemporal();
    }

    @Transactional
    public void modificarClave(String id, CambiarClaveRequest request) {
        modificarClave(id, request.claveActual(), request.nuevaClave(), request.confirmarClave());
    }

    @Transactional
    public void modificarClave(String id, String claveActual, String nuevaClave, String confirmarClave) {
        Usuario usuario = buscarUsuario(id);

        if (claveActual == null || !passwordEncoder.matches(claveActual, usuario.getClave())) {
            throw new ValidacionException("La clave actual no coincide");
        }
        if (nuevaClave == null || nuevaClave.length() < 8) {
            throw new ValidacionException("La nueva clave debe tener al menos 8 caracteres");
        }
        if (CLAVE_POR_DEFECTO.equals(nuevaClave)) {
            throw new ValidacionException("La nueva clave no puede ser la clave por defecto");
        }
        if (!nuevaClave.equals(confirmarClave)) {
            throw new ValidacionException("La nueva clave y la confirmación no coinciden");
        }

        usuario.setClave(passwordEncoder.encode(nuevaClave));
        usuario.setClaveTemporal(false);
        usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public PerfilResponse obtenerPerfil(String id) {
        Usuario usuario = buscarUsuario(id);
        return new PerfilResponse(
                toResponse(usuario),
                null,
                null,
                null
        );
    }

    public UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombreUsuario(),
                usuario.getRol(),
                usuario.isClaveTemporal(),
                usuario.getProveedorAuth()
        );
    }
}
