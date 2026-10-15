package com.example.zero.repositories;

import com.example.zero.entidades.persona.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, String> {

    default Optional<Empleado> find(String numeroDocumento) {
        return numeroDocumento != null ? findById(numeroDocumento) : Optional.empty();
    }

    default Optional<Empleado> find(UUID id) {
        return id != null ? find(id.toString()) : Optional.empty();
    }

    @Query("SELECT e FROM Empleado e WHERE e.numeroDocumento = :doc AND e.eliminado = false")
    Optional<Empleado> findActive(@Param("doc") String numeroDocumento);

    default Optional<Empleado> findActive(UUID id) {
        return id != null ? findActive(id.toString()) : Optional.empty();
    }

    Optional<Empleado> findByNumeroDocumentoAndEliminadoFalse(String numeroDocumento);

    List<Empleado> findByEliminadoFalse();
}

