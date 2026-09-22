package com.example.zero.repositories;

import com.example.zero.entidades.zona.Direccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DireccionRepository extends JpaRepository<Direccion, String> {

    default Optional<Direccion> find(String id) {
        return id != null ? findById(id) : Optional.empty();
    }

    default Optional<Direccion> find(UUID id) {
        return id != null ? find(id.toString()) : Optional.empty();
    }

    @Query("SELECT d FROM Direccion d WHERE d.id = :id AND d.eliminado = false")
    Optional<Direccion> findActive(@Param("id") String id);

    default Optional<Direccion> findActive(UUID id) {
        return id != null ? findActive(id.toString()) : Optional.empty();
    }

    List<Direccion> findByEliminadoFalse();
}

