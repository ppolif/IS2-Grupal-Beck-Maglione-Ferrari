package com.DulceMayor.backend.repositories.ruta;

import com.DulceMayor.backend.entities.ruta.Ruta;
import com.DulceMayor.backend.entities.ruta.RutaEstado;
import com.DulceMayor.backend.repositories.BaseRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para la jerarquía polimórfica de {@link Ruta}.
 */
@Repository
public interface RutaRepository extends BaseRepository<Ruta> {

    boolean existsByResponsableIdAndFechaAndEliminadoFalse(String responsableId, LocalDate fecha);

    Optional<Ruta> findByResponsableIdAndFechaAndEliminadoFalse(String responsableId, LocalDate fecha);

    @Query("""
        SELECT r FROM Ruta r
        WHERE r.eliminado = false
          AND (:tipo IS NULL OR TYPE(r) = :tipo)
          AND (:responsableId IS NULL OR r.responsable.id = :responsableId)
          AND (:fecha IS NULL OR r.fecha = :fecha)
          AND (:estado IS NULL OR r.estado = :estado)
        ORDER BY r.fecha DESC
    """)
    List<Ruta> buscarConFiltros(
            @Param("tipo") Class<? extends Ruta> tipo,
            @Param("responsableId") String responsableId,
            @Param("fecha") LocalDate fecha,
            @Param("estado") RutaEstado estado
    );
}

