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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
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

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtProvider jwtProvider) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.objectMapper = new ObjectMapper();
    }

    /// metodo de UserDetailsService
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByNombreUsuarioAndEliminadoFalse(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado o inactivo"));
        return new UsuarioPrincipal(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario buscarUsuario(String id) {
        return usuarioRepository.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarUsuarioActivo(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return usuarioRepository.findByIdAndEliminadoFalse(id);
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

    /// recibe request
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Usuario usuario = login(request.nombreUsuario(), request.clave());
        return generarAuthResponse(usuario);
    }

    /// procesa request y encuentra al usuario
    @Transactional(readOnly = true)
    public Usuario login(String nombreUsuario, String clave) {
        String usernameLimpio = nombreUsuario != null ? nombreUsuario.trim().toLowerCase() : "";
        Optional<Usuario> candidato = usuarioRepository.findByNombreUsuario(usernameLimpio);
        if (candidato.isEmpty()) {
            candidato = usuarioRepository.findByNombreUsuario(nombreUsuario != null ? nombreUsuario.trim() : "");
        }
        if (candidato.isEmpty()) {
            throw new CredencialesInvalidasException();
        }
        Usuario usuario = candidato.get();
        if (usuario.isEliminado() || !passwordEncoder.matches(clave, usuario.getClave())) {
            throw new CredencialesInvalidasException();
        }
        return usuario;
    }

    public record DatosTokenExterno(String email, String nombre, String apellido, String sub) {}

    public DatosTokenExterno parsearTokenExterno(String tokenExterno) {
        if (tokenExterno == null || tokenExterno.isBlank()) {
            throw new ValidacionException("El token de autenticación externo es obligatorio");
        }
        String tokenLimpio = tokenExterno.trim();
        String payloadJson = null;

        if (tokenLimpio.contains(".")) {
            String[] partes = tokenLimpio.split("\\.");
            if (partes.length >= 2) {
                try {
                    byte[] decoded = Base64.getUrlDecoder().decode(partes[1]);
                    payloadJson = new String(decoded, StandardCharsets.UTF_8);
                } catch (IllegalArgumentException e) {
                    payloadJson = null;
                }
            }
        }

        if (payloadJson == null) {
            payloadJson = tokenLimpio;
        }

        String email = null;
        String nombre = "Cliente";
        String apellido = "";
        String sub = null;

        try {
            JsonNode node = objectMapper.readTree(payloadJson);
            if (node.hasNonNull("email")) {
                email = node.get("email").asText().trim().toLowerCase();
            } else if (node.hasNonNull("name") && node.get("name").asText().contains("@")) {
                email = node.get("name").asText().trim().toLowerCase();
            }
            if (node.hasNonNull("given_name")) {
                nombre = node.get("given_name").asText().trim();
            } else if (node.hasNonNull("name")) {
                String fullName = node.get("name").asText().trim();
                String[] words = fullName.split("\\s+", 2);
                nombre = words[0];
                if (words.length > 1) {
                    apellido = words[1];
                }
            }
            if (node.hasNonNull("family_name")) {
                apellido = node.get("family_name").asText().trim();
            }
            if (node.hasNonNull("sub")) {
                sub = node.get("sub").asText().trim();
            }
        } catch (Exception e) {
            if (payloadJson.contains("@") && !payloadJson.contains(" ")) {
                email = payloadJson.trim().toLowerCase();
            }
        }

        if (email == null || email.isBlank()) {
            throw new ValidacionException("No se pudo obtener el correo electrónico a partir del token del proveedor externo");
        }

        return new DatosTokenExterno(email, nombre, apellido, sub);
    }

    @Transactional
    public Usuario loginExterno(ProveedorAuth proveedor, String tokenExterno) {
        if (proveedor == null) {
            throw new ValidacionException("El proveedor de autenticación es obligatorio");
        }
        DatosTokenExterno datos = parsearTokenExterno(tokenExterno);
        String email = datos.email();

        Optional<Usuario> existente = usuarioRepository.findByNombreUsuario(email);
        if (existente.isPresent()) {
            Usuario usuario = existente.get();
            if (usuario.isEliminado()) {
                throw new CredencialesInvalidasException();
            }
            if (usuario.getProveedorAuth() == null || usuario.getProveedorAuth() != ProveedorAuth.LOCAL) {
                usuario.setProveedorAuth(proveedor);
                usuarioRepository.save(usuario);
            }
            return usuario;
        }

        Usuario nuevo = Usuario.builder()
                .nombreUsuario(email)
                .clave(passwordEncoder.encode(UUID.randomUUID().toString()))
                .rol(RolUsuario.MINORISTA)
                .claveTemporal(false)
                .proveedorAuth(proveedor)
                .eliminado(false)
                .build();

        return usuarioRepository.save(nuevo);
    }

    @Transactional(readOnly = true)
    public boolean requiereCambioClave(String id) {
        return buscarUsuario(id).isClaveTemporal();
    }

    /// metodo que recibe la request
    @Transactional
    public void modificarClave(String id, CambiarClaveRequest request) {
        modificarClave(id, request.claveActual(), request.nuevaClave(), request.confirmarClave());
    }

    /// metodo que valida y hace el cambio efectivo de contraseña
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

    @Transactional(readOnly = true)
    public boolean existeNombreUsuario(String nombreUsuario) {
        return usuarioRepository.existsByNombreUsuario(nombreUsuario);
    }

    @Transactional(readOnly = true)
    public boolean existeNombreUsuarioExcluyendoId(String nombreUsuario, String id) {
        return usuarioRepository.existsByNombreUsuarioAndIdNot(nombreUsuario, id);
    }

    @Transactional
    public Usuario crearUsuario(String nombreUsuario, String clave, RolUsuario rol, boolean claveTemporal) {
        Usuario usuario = Usuario.builder()
                .nombreUsuario(nombreUsuario)
                .clave(passwordEncoder.encode(clave))
                .rol(rol)
                .claveTemporal(claveTemporal)
                .proveedorAuth(ProveedorAuth.LOCAL)
                .eliminado(false)
                .build();
        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public AuthResponse generarAuthResponse(Usuario usuario) {
        String token = jwtProvider.generarToken(usuario);
        return new AuthResponse(
                token,
                "Bearer",
                jwtProvider.getExpiraEnSegundos(),
                usuario.isClaveTemporal(),
                toResponse(usuario)
        );
    }

    @Transactional
    public void actualizarUsuarioEmpleado(Usuario usuario, String nuevoNombreUsuario, RolUsuario nuevoRol) {
        usuario.setNombreUsuario(nuevoNombreUsuario);
        usuario.setRol(nuevoRol);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void actualizarNombreUsuario(Usuario usuario, String nuevoNombreUsuario) {
        if (usuario != null && nuevoNombreUsuario != null) {
            usuario.setNombreUsuario(nuevoNombreUsuario.trim().toLowerCase());
            usuarioRepository.save(usuario);
        }
    }

    @Transactional
    public void eliminarUsuario(String id) {
        if (id == null) return;
        usuarioRepository.findById(id).ifPresent(u -> {
            u.setEliminado(true);
            usuarioRepository.save(u);
        });
    }
}
