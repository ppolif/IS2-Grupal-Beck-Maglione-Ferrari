package com.example.carrito.repositorios;

import com.example.carrito.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepositorio extends JpaRepository<Usuario, String> {
    Optional<Usuario> findByEmailAndEliminadoFalse(String email);
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmailAndEliminadoFalse(String email);
}

