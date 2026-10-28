package com.example.carrito.repositorios;

import com.example.carrito.entidades.RegistroCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistroCompraRepositorio extends JpaRepository<RegistroCompra, String> {
    List<RegistroCompra> findByUsuarioIdAndEliminadoFalseOrderByFechaDesc(String usuarioId);
}

