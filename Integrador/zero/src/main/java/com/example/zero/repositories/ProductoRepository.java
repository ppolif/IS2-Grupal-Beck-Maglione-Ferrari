package com.example.zero.repositories;

import com.example.zero.entidades.producto.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, String> {

    default Optional<Producto> find(String id) {
        return id != null ? findById(id) : Optional.empty();
    }

    default Optional<Producto> find(UUID id) {
        return id != null ? find(id.toString()) : Optional.empty();
    }

    @Query("SELECT p FROM Producto p WHERE p.id = :id AND p.eliminado = false")
    Optional<Producto> findActive(@Param("id") String id);

    default Optional<Producto> findActive(UUID id) {
        return id != null ? findActive(id.toString()) : Optional.empty();
    }

    Optional<Producto> findByCodigoAndEliminadoFalse(String codigo);

    Optional<Producto> findByCodigo(String codigo);

    List<Producto> findByEliminadoFalse();

    List<Producto> findByEnOfertaTrueAndEliminadoFalse();

    List<Producto> findBySubCategoriaIdAndEliminadoFalse(String subCategoriaId);
}

