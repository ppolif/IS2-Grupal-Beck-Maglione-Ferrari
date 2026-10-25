package com.example.zero.repositories;

import com.example.zero.entidades.persona.Nacionalidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NacionalidadRepository extends JpaRepository<Nacionalidad, String> {

    default Optional<Nacionalidad> find(String id) {
        return id != null ? findById(id) : Optional.empty();
    }

    default Optional<Nacionalidad> find(UUID id) {
        return id != null ? find(id.toString()) : Optional.empty();
    }

    @Query("SELECT n FROM Nacionalidad n WHERE n.id = :id AND n.eliminado = false")
    Optional<Nacionalidad> findActive(@Param("id") String id);

    default Optional<Nacionalidad> findActive(UUID id) {
        return id != null ? findActive(id.toString()) : Optional.empty();
    }

    Optional<Nacionalidad> findByNombreAndEliminadoFalse(String nombre);

    List<Nacionalidad> findByEliminadoFalse();
}

