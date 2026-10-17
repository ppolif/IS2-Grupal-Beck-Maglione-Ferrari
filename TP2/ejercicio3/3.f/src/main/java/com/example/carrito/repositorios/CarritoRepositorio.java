package com.example.carrito.repositorios;

import com.example.carrito.entidades.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarritoRepositorio extends JpaRepository<Carrito, String> {
    Optional<Carrito> findByUsuarioIdAndEliminadoFalse(String usuarioId);
}

