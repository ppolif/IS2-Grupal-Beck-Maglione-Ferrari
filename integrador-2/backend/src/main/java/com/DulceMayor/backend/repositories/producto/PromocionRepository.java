package com.DulceMayor.backend.repositories.producto;

import com.DulceMayor.backend.entities.producto.Promocion;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.DulceMayor.backend.repositories.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de acceso a datos para la entidad {@link Promocion}.
 */
@Repository
public interface PromocionRepository extends BaseRepository<Promocion> {

    Optional<Promocion> findByCodigoIgnoreCaseAndEliminadoFalse(String codigo);

    boolean existsByCodigoIgnoreCaseAndEliminadoFalse(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNotAndEliminadoFalse(String codigo, String id);

    List<Promocion> findAllByEliminadoFalseOrderByCodigoAsc();

    ///buscar promociones que estaban vigentes en una fecha determinada
    @Query("""
            SELECT p FROM Promocion p
            WHERE p.eliminado = false
              AND p.fechaDesde <= :fecha
              AND p.fechaHasta >= :fecha
            ORDER BY p.codigo ASC
            """)
    List<Promocion> findAllVigentes(@Param("fecha") LocalDate fecha);
}

