package com.example.zero.repositories;

import com.example.zero.entidades.producto.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, String> {

    default Optional<Categoria> find(String id) {
        return id != null ? findById(id) : Optional.empty();
    }

    default Optional<Categoria> find(UUID id) {
        return id != null ? find(id.toString()) : Optional.empty();
    }

    @Query("SELECT c FROM Categoria c WHERE c.id = :id AND c.eliminado = false")
    Optional<Categoria> findActive(@Param("id") String id);

    default Optional<Categoria> findActive(UUID id) {
        return id != null ? findActive(id.toString()) : Optional.empty();
    }

    Optional<Categoria> findByNombre(String nombre);

    Optional<Categoria> findByNombreAndEliminadoFalse(String nombre);

    List<Categoria> findByEliminadoFalse();
}

