package com.example.zero.repositories;

import com.example.zero.entidades.compra.Detalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DetalleRepository extends JpaRepository<Detalle, String> {

    default Optional<Detalle> find(String id) {
        return id != null ? findById(id) : Optional.empty();
    }

    default Optional<Detalle> find(UUID id) {
        return id != null ? find(id.toString()) : Optional.empty();
    }

    @Query("SELECT d FROM Detalle d WHERE d.id = :id AND d.eliminado = false")
    Optional<Detalle> findActive(@Param("id") String id);

    default Optional<Detalle> findActive(UUID id) {
        return id != null ? findActive(id.toString()) : Optional.empty();
    }

    List<Detalle> findByFacturaIdAndEliminadoFalse(String facturaId);
}

