package com.example.zero.repositories;

import com.example.zero.entidades.zona.Provincia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProvinciaRepository extends JpaRepository<Provincia, String> {

    default Optional<Provincia> find(String id) {
        return id != null ? findById(id) : Optional.empty();
    }

    default Optional<Provincia> find(UUID id) {
        return id != null ? find(id.toString()) : Optional.empty();
    }

    @Query("SELECT pr FROM Provincia pr WHERE pr.id = :id AND pr.eliminado = false")
    Optional<Provincia> findActive(@Param("id") String id);

    default Optional<Provincia> findActive(UUID id) {
        return id != null ? findActive(id.toString()) : Optional.empty();
    }

    List<Provincia> findByPaisIdAndEliminadoFalse(String paisId);

    List<Provincia> findByEliminadoFalse();

    Optional<Provincia> findByNombreAndPaisIdAndEliminadoFalse(String nombre, String paisId);
}

