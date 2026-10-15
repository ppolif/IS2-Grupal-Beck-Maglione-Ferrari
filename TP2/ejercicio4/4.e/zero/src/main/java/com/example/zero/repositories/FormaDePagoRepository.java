package com.example.zero.repositories;

import com.example.zero.entidades.compra.FormaDePago;
import com.example.zero.enums.TipoDePago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FormaDePagoRepository extends JpaRepository<FormaDePago, String> {

    default Optional<FormaDePago> find(String id) {
        return id != null ? findById(id) : Optional.empty();
    }

    default Optional<FormaDePago> find(UUID id) {
        return id != null ? find(id.toString()) : Optional.empty();
    }

    @Query("SELECT fp FROM FormaDePago fp WHERE fp.id = :id AND fp.eliminado = false")
    Optional<FormaDePago> findActive(@Param("id") String id);

    default Optional<FormaDePago> findActive(UUID id) {
        return id != null ? findActive(id.toString()) : Optional.empty();
    }

    Optional<FormaDePago> findByTipoPagoAndEliminadoFalse(TipoDePago tipoPago);

    List<FormaDePago> findByEliminadoFalse();
}

