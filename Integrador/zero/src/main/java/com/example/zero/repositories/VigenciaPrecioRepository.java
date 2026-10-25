package com.example.zero.repositories;

import com.example.zero.entidades.producto.VigenciaPrecio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VigenciaPrecioRepository extends JpaRepository<VigenciaPrecio, String> {

    default Optional<VigenciaPrecio> find(String id) {
        return id != null ? findById(id) : Optional.empty();
    }

    default Optional<VigenciaPrecio> find(UUID id) {
        return id != null ? find(id.toString()) : Optional.empty();
    }

    @Query("SELECT v FROM VigenciaPrecio v WHERE v.id = :id AND v.eliminado = false")
    Optional<VigenciaPrecio> findActive(@Param("id") String id);

    default Optional<VigenciaPrecio> findActive(UUID id) {
        return id != null ? findActive(id.toString()) : Optional.empty();
    }

    // Precio vigente actual: cuando fechaHasta es null y no está eliminado
    @Query("SELECT v FROM VigenciaPrecio v WHERE v.producto.id = :productoId AND v.fechaHasta IS NULL AND v.eliminado = false")
    Optional<VigenciaPrecio> findPrecioActualByProductoId(@Param("productoId") String productoId);

    // Historial ordenado por fechaDesde descendente
    List<VigenciaPrecio> findByProductoIdAndEliminadoFalseOrderByFechaDesdeDesc(String productoId);

    List<VigenciaPrecio> findByProductoIdAndEliminadoFalse(String productoId);
}

