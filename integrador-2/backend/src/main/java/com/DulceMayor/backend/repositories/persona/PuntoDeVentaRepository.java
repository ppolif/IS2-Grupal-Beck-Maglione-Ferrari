package com.DulceMayor.backend.repositories.persona;

import com.DulceMayor.backend.entities.persona.PuntoDeVenta;
import com.DulceMayor.backend.repositories.BaseRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para la entidad {@link PuntoDeVenta}.
 */
@Repository
public interface PuntoDeVentaRepository extends BaseRepository<PuntoDeVenta> {

    List<PuntoDeVenta> findAllByMinoristaIdAndEliminadoFalse(String minoristaId);

    List<PuntoDeVenta> findAllByPreventorIdAndEliminadoFalse(String preventorId);

    @Query("""
        SELECT p FROM PuntoDeVenta p
        JOIN p.minorista m
        LEFT JOIN p.preventor pr
        WHERE p.eliminado = false
          AND (:minoristaId IS NULL OR m.id = :minoristaId)
          AND (:preventorId IS NULL OR pr.id = :preventorId)
          AND (:q IS NULL OR (
               LOWER(p.nombreComercio) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(m.nombre) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(m.apellido) LIKE LOWER(CONCAT('%', :q, '%'))
          ))
        ORDER BY p.nombreComercio ASC
    """)
    Page<PuntoDeVenta> buscarConFiltros(
            @Param("minoristaId") String minoristaId,
            @Param("preventorId") String preventorId,
            @Param("q") String q,
            Pageable pageable);
}

