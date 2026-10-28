package com.DulceMayor.backend.repositories.producto;

import com.DulceMayor.backend.entities.producto.VigenciaPrecio;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.DulceMayor.backend.repositories.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de acceso a datos para la entidad {@link VigenciaPrecio}.
 */
@Repository
public interface VigenciaPrecioRepository extends BaseRepository<VigenciaPrecio> {

    List<VigenciaPrecio> findAllByProductoIdAndEliminadoFalseOrderByFechaDesdeDesc(String productoId);

    Optional<VigenciaPrecio> findByProductoIdAndFechaHastaIsNullAndEliminadoFalse(String productoId);

    List<VigenciaPrecio> findAllByProductoIdAndEliminadoFalse(String productoId);

    @Query("""
            SELECT v FROM VigenciaPrecio v
            WHERE v.producto.id = :productoId
              AND v.eliminado = false
              AND (:idExcluir IS NULL OR v.id != :idExcluir)
              AND (
                   (:fechaHasta IS NULL AND (v.fechaHasta IS NULL OR v.fechaHasta >= :fechaDesde))
                   OR
                   (:fechaHasta IS NOT NULL AND v.fechaDesde <= :fechaHasta AND (v.fechaHasta IS NULL OR v.fechaHasta >= :fechaDesde))
              )
            """)
    List<VigenciaPrecio> buscarSolapamientos(
            @Param("productoId") String productoId,
            @Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta,
            @Param("idExcluir") String idExcluir
    );
}

