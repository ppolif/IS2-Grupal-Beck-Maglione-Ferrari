package com.example.zero.repositories;

import com.example.zero.entidades.compraCliente.DetalleCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DetalleCompraRepository extends JpaRepository<DetalleCompra, String> {

    default Optional<DetalleCompra> find(String id) {
        return id != null ? findById(id) : Optional.empty();
    }

    default Optional<DetalleCompra> find(UUID id) {
        return id != null ? find(id.toString()) : Optional.empty();
    }

    @Query("SELECT d FROM DetalleCompra d WHERE d.id = :id AND d.eliminado = false")
    Optional<DetalleCompra> findActive(@Param("id") String id);

    default Optional<DetalleCompra> findActive(UUID id) {
        return id != null ? findActive(id.toString()) : Optional.empty();
    }

    List<DetalleCompra> findByOrdenCompraIdAndEliminadoFalse(String ordenCompraId);
}
<<<<<<< HEAD
=======

>>>>>>> augusto
