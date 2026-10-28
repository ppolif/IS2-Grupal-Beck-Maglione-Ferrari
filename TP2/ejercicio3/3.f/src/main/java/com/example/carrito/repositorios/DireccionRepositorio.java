package com.example.carrito.repositorios;

import com.example.carrito.entidades.Direccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para realizar operaciones CRUD sobre la entidad Direccion.
 */
@Repository
public interface DireccionRepositorio extends JpaRepository<Direccion, String> {
}

