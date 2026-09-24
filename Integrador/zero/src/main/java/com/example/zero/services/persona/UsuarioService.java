package com.example.zero.services.persona;

import com.example.zero.entidades.persona.Persona;
import com.example.zero.entidades.persona.Usuario;
import com.example.zero.enums.RolUsuario;
import com.example.zero.repositories.UsuarioRepository;
import com.example.zero.services.mail.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this(usuarioRepository, null);
    }

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository, EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
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
        return crearUsuario(nombreUsuario, clave, rol, persona, true);
    }

    @Transactional
    public Usuario crearUsuario(String nombreUsuario, String clave, RolUsuario rol, Persona persona, boolean activo) {
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
                .activo(activo)
                .eliminado(false)
                .build();

        return usuarioRepository.save(usuario);
    }

    public String generarCodigoAleatorio() {
        Random random = new Random();
        int numero = 100000 + random.nextInt(900000);
        return String.valueOf(numero);
    }

    @Transactional
    public String generarYAsignarCodigo(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("El correo electrónico no puede estar vacío");
        }
        String emailLimpio = email.trim().toLowerCase();
        Usuario usuario = usuarioRepository.findByNombreUsuarioAndEliminadoFalse(emailLimpio)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró ningún usuario con el correo: " + emailLimpio));

        if (usuario.isActivo()) {
            throw new IllegalArgumentException("La cuenta ya se encuentra activa");
        }

        String codigo = generarCodigoAleatorio();
        usuario.setCodigoConfirmacion(codigo);
        usuario.setCodigoExpiracion(LocalDateTime.now().plusMinutes(15));
        usuarioRepository.save(usuario);
        return codigo;
    }

    @Transactional
    public void enviarCodigoConfirmacion(String email, String codigo) {
        if (emailService != null) {
            emailService.enviarCodigoConfirmacion(email.trim().toLowerCase(), codigo);
        }
    }

    @Transactional
    public void reenviarCodigoConfirmacion(String email) {
        String codigo = generarYAsignarCodigo(email);
        enviarCodigoConfirmacion(email, codigo);
    }

    @Transactional
    public Usuario verificarCodigo(String email, String codigo) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("El correo electrónico no puede estar vacío");
        }
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar el código de confirmación");
        }

        String emailLimpio = email.trim().toLowerCase();
        Usuario usuario = usuarioRepository.findByNombreUsuarioAndEliminadoFalse(emailLimpio)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró una cuenta asociada al correo: " + emailLimpio));

        if (usuario.isActivo()) {
            return usuario;
        }

        if (usuario.getCodigoConfirmacion() == null || usuario.getCodigoExpiracion() == null) {
            throw new IllegalArgumentException("No hay ningún código de confirmación pendiente. Solicita uno nuevo.");
        }

        if (LocalDateTime.now().isAfter(usuario.getCodigoExpiracion())) {
            throw new IllegalArgumentException("El código de confirmación ha expirado. Por favor solicita uno nuevo.");
        }

        if (!usuario.getCodigoConfirmacion().equals(codigo.trim())) {
            throw new IllegalArgumentException("El código de confirmación ingresado es incorrecto.");
        }

        usuario.setActivo(true);
        usuario.setCodigoConfirmacion(null);
        usuario.setCodigoExpiracion(null);
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

        if (!usuario.isActivo()) {
            throw new IllegalArgumentException("Tu cuenta no está activa. Debes confirmar el código enviado a tu correo.");
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
    public Usuario actualizarPerfil(String id, String nuevoEmail, String nuevaFoto) {
        Usuario usuario = buscarPorId(id);

        if (nuevoEmail != null && !nuevoEmail.trim().isEmpty()) {
            String emailLimpio = nuevoEmail.trim().toLowerCase();
            if (!emailLimpio.contains("@")) {
                throw new IllegalArgumentException("El formato del correo electrónico es inválido");
            }
            if (!emailLimpio.equalsIgnoreCase(usuario.getNombreUsuario())) {
                usuarioRepository.findByNombreUsuarioAndEliminadoFalse(emailLimpio).ifPresent(existente -> {
                    if (!existente.getId().equals(usuario.getId())) {
                        throw new IllegalArgumentException("El correo electrónico ya se encuentra registrado por otro usuario.");
                    }
                });
                usuario.setNombreUsuario(emailLimpio);

                if (usuario.getPersona() != null && usuario.getPersona().getContactos() != null) {
                    for (var c : usuario.getPersona().getContactos()) {
                        if (c instanceof com.example.zero.entidades.empresa.ContactoCorreoElectronico ce) {
                            ce.setEmail(emailLimpio);
                        }
                    }
                }
            }
        }

        if (nuevaFoto != null) {
            usuario.setFoto(nuevaFoto.trim());
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
