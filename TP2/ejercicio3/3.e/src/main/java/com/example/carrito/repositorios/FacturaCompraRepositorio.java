package com.example.carrito.repositorios;

import com.example.carrito.entidades.FacturaCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacturaCompraRepositorio extends JpaRepository<FacturaCompra, String> {
    List<FacturaCompra> findByUsuarioIdAndEliminadoFalseOrderByFechaDesc(String usuarioId);
}
