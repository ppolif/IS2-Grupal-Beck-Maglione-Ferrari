package com.example.zero.repositories;

import com.example.zero.entidades.zona.Departamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepartamentoRepository extends JpaRepository<Departamento, String> {

    default Optional<Departamento> find(String id) {
        return id != null ? findById(id) : Optional.empty();
    }

    default Optional<Departamento> find(UUID id) {
        return id != null ? find(id.toString()) : Optional.empty();
    }

    @Query("SELECT d FROM Departamento d WHERE d.id = :id AND d.eliminado = false")
    Optional<Departamento> findActive(@Param("id") String id);

    default Optional<Departamento> findActive(UUID id) {
        return id != null ? findActive(id.toString()) : Optional.empty();
    }

    List<Departamento> findByProvinciaIdAndEliminadoFalse(String provinciaId);

    List<Departamento> findByEliminadoFalse();

    Optional<Departamento> findByNombreAndProvinciaIdAndEliminadoFalse(String nombre, String provinciaId);
}

