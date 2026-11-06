package com.DulceMayor.backend.repositories.ruta;

import com.DulceMayor.backend.entities.ruta.RutaEstado;
import com.DulceMayor.backend.entities.ruta.RutaPreventor;
import com.DulceMayor.backend.repositories.BaseRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA específico para {@link RutaPreventor}.
 */
@Repository
public interface RutaPreventorRepository extends BaseRepository<RutaPreventor> {

    boolean existsByResponsableIdAndFechaAndEliminadoFalse(String responsableId, LocalDate fecha);

    Optional<RutaPreventor> findByResponsableIdAndFechaAndEliminadoFalse(String preventorId, LocalDate fecha);

    @Query("""
        SELECT r FROM RutaPreventor r
        WHERE r.eliminado = false
          AND (:responsableId IS NULL OR r.responsable.id = :responsableId)
          AND (:fecha IS NULL OR r.fecha = :fecha)
          AND (:estado IS NULL OR r.estado = :estado)
        ORDER BY r.fecha DESC
    """)
    List<RutaPreventor> buscarConFiltros(
            @Param("responsableId") String responsableId,
            @Param("fecha") LocalDate fecha,
            @Param("estado") RutaEstado estado
    );
}

