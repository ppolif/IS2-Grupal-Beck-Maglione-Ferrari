package com.example.zero.repositories;

import com.example.zero.entidades.compra.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, String> {

    default Optional<Factura> find(String id) {
        return id != null ? findById(id) : Optional.empty();
    }

    default Optional<Factura> find(UUID id) {
        return id != null ? find(id.toString()) : Optional.empty();
    }

    @Query("SELECT f FROM Factura f WHERE f.id = :id AND f.eliminado = false")
    Optional<Factura> findActive(@Param("id") String id);

    default Optional<Factura> findActive(UUID id) {
        return id != null ? findActive(id.toString()) : Optional.empty();
    }

    Optional<Factura> findByNumeroFacturaAndEliminadoFalse(Long numeroFactura);

    List<Factura> findByEliminadoFalseOrderByFechaFacturaDesc();

    Optional<Factura> findTopByOrderByNumeroFacturaDesc();
}

