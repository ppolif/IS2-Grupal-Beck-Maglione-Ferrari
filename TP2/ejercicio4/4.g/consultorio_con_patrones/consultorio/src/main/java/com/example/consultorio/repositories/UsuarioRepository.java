package com.example.consultorio.repositories;

import com.example.consultorio.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {
    Optional<Usuario> findByNombreUsuarioAndEliminadoFalse(String nombreUsuario);
}
