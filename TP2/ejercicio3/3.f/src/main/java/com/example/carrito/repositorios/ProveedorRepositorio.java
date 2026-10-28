package com.example.carrito.repositorios;

import com.example.carrito.entidades.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProveedorRepositorio extends JpaRepository<Proveedor, String> {
    List<Proveedor> findAllByEliminadoFalse();
    Optional<Proveedor> findByNombreIgnoreCaseAndEliminadoFalse(String nombre);
    boolean existsByNombreIgnoreCaseAndEliminadoFalse(String nombre);
}

