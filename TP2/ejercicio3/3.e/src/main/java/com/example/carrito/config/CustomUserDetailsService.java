package com.example.carrito.config;

import com.example.carrito.entidades.Usuario;
import com.example.carrito.repositorios.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepositorio.findByEmailAndEliminadoFalse(email.trim().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("No existe usuario con el correo: " + email));

        String rolSpring = "ROLE_" + usuario.getRol().name();

        return new User(
                usuario.getEmail(),
                usuario.getClave(),
                Collections.singletonList(new SimpleGrantedAuthority(rolSpring))
        );
    }
}

