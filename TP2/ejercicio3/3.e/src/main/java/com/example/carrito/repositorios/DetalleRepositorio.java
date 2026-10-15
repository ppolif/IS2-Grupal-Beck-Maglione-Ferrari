package com.example.carrito.repositorios;

import com.example.carrito.entidades.Detalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DetalleRepositorio extends JpaRepository<Detalle, String> {
    List<Detalle> findByCarritoIdAndEliminadoFalse(String carritoId);
    Optional<Detalle> findByIdAndEliminadoFalse(String id);
}

