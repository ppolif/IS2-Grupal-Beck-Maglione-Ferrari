package com.example.carrito.repositorios;

import com.example.carrito.entidades.Articulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticuloRepositorio extends JpaRepository<Articulo, String> {
    List<Articulo> findAllByEliminadoFalse();
    Optional<Articulo> findByIdAndEliminadoFalse(String id);
}

