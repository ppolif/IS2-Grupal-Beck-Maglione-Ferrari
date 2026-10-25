package com.example.zero.repositories;

import com.example.zero.entidades.zona.Pais;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaisRepository extends JpaRepository<Pais, String> {

    default Optional<Pais> find(String id) {
        return id != null ? findById(id) : Optional.empty();
    }

    default Optional<Pais> find(UUID id) {
        return id != null ? find(id.toString()) : Optional.empty();
    }

    @Query("SELECT p FROM Pais p WHERE p.id = :id AND p.eliminado = false")
    Optional<Pais> findActive(@Param("id") String id);

    default Optional<Pais> findActive(UUID id) {
        return id != null ? findActive(id.toString()) : Optional.empty();
    }

    Pais findByNombre(String nombre);

    Optional<Pais> findByNombreAndEliminadoFalse(String nombre);

    List<Pais> findByEliminadoFalse();
}