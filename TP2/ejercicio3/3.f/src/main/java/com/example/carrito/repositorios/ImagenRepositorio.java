package com.example.carrito.repositorios;

import com.example.carrito.entidades.Imagen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImagenRepositorio extends JpaRepository<Imagen, String> {
    Optional<Imagen> findByIdAndEliminadoFalse(String id);
}

