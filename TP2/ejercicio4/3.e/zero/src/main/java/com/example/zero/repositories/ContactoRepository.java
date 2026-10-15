package com.example.zero.repositories;

import com.example.zero.entidades.empresa.Contacto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContactoRepository extends JpaRepository<Contacto, String> {

    default Optional<Contacto> find(String id) {
        return id != null ? findById(id) : Optional.empty();
    }

    default Optional<Contacto> find(UUID id) {
        return id != null ? find(id.toString()) : Optional.empty();
    }

    @Query("SELECT c FROM Contacto c WHERE c.id = :id AND c.eliminado = false")
    Optional<Contacto> findActive(@Param("id") String id);

    default Optional<Contacto> findActive(UUID id) {
        return id != null ? findActive(id.toString()) : Optional.empty();
    }

    List<Contacto> findByEliminadoFalse();
}

