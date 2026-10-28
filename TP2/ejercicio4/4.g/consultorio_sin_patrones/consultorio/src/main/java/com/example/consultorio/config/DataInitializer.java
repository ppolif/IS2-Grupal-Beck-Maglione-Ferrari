package com.example.consultorio.config;

import com.example.consultorio.entities.Usuario;
import com.example.consultorio.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner; // <-- Importante
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

//hacemos un admin inicial (aunque al final no hacia falta porque puse registro lol)
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String userAdminSemilla = "admin";
    private String claveAdminSemilla = "1234";

    @Override
    @Transactional
    public void run(String... args) {
        Optional<Usuario> adminExiste = usuarioRepository.findByNombreUsuarioAndEliminadoFalse(userAdminSemilla);
        if (adminExiste.isPresent()) {
            return;
        }

        Usuario admin = new Usuario();
        admin.setNombreUsuario(userAdminSemilla);
        admin.setClave(passwordEncoder.encode(claveAdminSemilla));
        admin.setEliminado(false);
        usuarioRepository.save(admin);
    }
}
