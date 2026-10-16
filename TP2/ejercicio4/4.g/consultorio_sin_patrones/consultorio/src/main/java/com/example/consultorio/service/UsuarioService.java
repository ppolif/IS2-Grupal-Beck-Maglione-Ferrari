package com.example.consultorio.service;

import com.example.consultorio.entities.Usuario;
import com.example.consultorio.repositories.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Service
public class UsuarioService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private HttpSession session;

    @Transactional
    public Usuario registrarUsuario(String nombreUsuario, String clave) {
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio.");
        }
        if (clave == null || clave.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña es obligatoria.");
        }

        Optional<Usuario> usuarioExistente = usuarioRepository.findByNombreUsuarioAndEliminadoFalse(nombreUsuario);
        if (usuarioExistente.isPresent()) {
            throw new IllegalStateException("El nombre de usuario ya se encuentra registrado.");
        }

        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(nombreUsuario);
        usuario.setClave(passwordEncoder.encode(clave));
        usuario.setEliminado(false);

        return usuarioRepository.save(usuario);
    }

    //este metodo lo maneja spring boot para el inicio de sesion
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByNombreUsuarioAndEliminadoFalse(username)
                        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // guardar el objeto en la sesión HTTP para validarSesion() en los templates
        session.setAttribute("usuariosession", usuario);

        return new User(
                usuario.getNombreUsuario(),
                usuario.getClave(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMINISTRATIVO"))
        );
    }

    public Optional<Usuario> buscarPorNombreUsuario(String username) {
        return usuarioRepository.findByNombreUsuarioAndEliminadoFalse(username);
    }
}
