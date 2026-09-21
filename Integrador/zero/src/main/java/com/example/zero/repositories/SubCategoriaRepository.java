package com.example.zero.repositories;

import com.example.zero.entidades.producto.SubCategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubCategoriaRepository extends JpaRepository<SubCategoria, String> {

    default Optional<SubCategoria> find(String id) {
        return id != null ? findById(id) : Optional.empty();
    }

    default Optional<SubCategoria> find(UUID id) {
        return id != null ? find(id.toString()) : Optional.empty();
    }

    @Query("SELECT s FROM SubCategoria s WHERE s.id = :id AND s.eliminado = false")
    Optional<SubCategoria> findActive(@Param("id") String id);

    default Optional<SubCategoria> findActive(UUID id) {
        return id != null ? findActive(id.toString()) : Optional.empty();
    }

    Optional<SubCategoria> findByNombreAndEliminadoFalse(String nombre);

    List<SubCategoria> findByCategoriaIdAndEliminadoFalse(String categoriaId);

    List<SubCategoria> findByEliminadoFalse();
}

