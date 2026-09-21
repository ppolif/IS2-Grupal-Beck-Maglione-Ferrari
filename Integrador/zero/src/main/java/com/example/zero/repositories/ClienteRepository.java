package com.example.zero.repositories;

import com.example.zero.entidades.persona.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, String> {

    default Optional<Cliente> find(String numeroDocumento) {
        return numeroDocumento != null ? findById(numeroDocumento) : Optional.empty();
    }

    default Optional<Cliente> find(UUID id) {
        return id != null ? find(id.toString()) : Optional.empty();
    }

    @Query("SELECT c FROM Cliente c WHERE c.numeroDocumento = :doc AND c.eliminado = false")
    Optional<Cliente> findActive(@Param("doc") String numeroDocumento);

    default Optional<Cliente> findActive(UUID id) {
        return id != null ? findActive(id.toString()) : Optional.empty();
    }

    Optional<Cliente> findByNumeroDocumentoAndEliminadoFalse(String numeroDocumento);

    List<Cliente> findByEliminadoFalse();
}

