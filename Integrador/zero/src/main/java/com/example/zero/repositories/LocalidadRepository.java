package com.example.zero.repositories;

import com.example.zero.entidades.zona.Localidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LocalidadRepository extends JpaRepository<Localidad, String> {

    default Optional<Localidad> find(String id) {
        return id != null ? findById(id) : Optional.empty();
    }

    default Optional<Localidad> find(UUID id) {
        return id != null ? find(id.toString()) : Optional.empty();
    }

    @Query("SELECT loc FROM Localidad loc WHERE loc.id = :id AND loc.eliminado = false")
    Optional<Localidad> findActive(@Param("id") String id);

    default Optional<Localidad> findActive(UUID id) {
        return id != null ? findActive(id.toString()) : Optional.empty();
    }

    List<Localidad> findByDepartamentoIdAndEliminadoFalse(String departamentoId);

    List<Localidad> findByEliminadoFalse();

    Optional<Localidad> findByNombreAndDepartamentoIdAndEliminadoFalse(String nombre, String departamentoId);
}

