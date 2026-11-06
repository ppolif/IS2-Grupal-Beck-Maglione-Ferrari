package com.DulceMayor.backend.repositories.persona;

import com.DulceMayor.backend.entities.RolUsuario;
import com.DulceMayor.backend.entities.persona.Empleado;
import com.DulceMayor.backend.repositories.BaseRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para la entidad {@link Empleado} y sus subclases.
 */
@Repository
public interface EmpleadoRepository extends BaseRepository<Empleado> {

    @Query("SELECT e FROM Empleado e JOIN e.usuarios u WHERE u.id = :usuarioId AND e.eliminado = false")
    Optional<Empleado> findByUsuarioIdAndEliminadoFalse(@Param("usuarioId") String usuarioId);

    Optional<Empleado> findByLegajoAndEliminadoFalse(String legajo);

    boolean existsByLegajoAndEliminadoFalse(String legajo);

    boolean existsByLegajoAndIdNotAndEliminadoFalse(String legajo, String id);

    @Query("SELECT e FROM Empleado e WHERE TYPE(e) = :tipo AND e.eliminado = false")
    <T extends Empleado> java.util.List<T> findAllByTipoAndEliminadoFalse(@Param("tipo") Class<T> tipo);

    @Query("""
        SELECT e FROM Empleado e
        LEFT JOIN e.usuarios u
        WHERE e.eliminado = false
          AND (:rol IS NULL OR (u.rol = :rol AND u.eliminado = false))
          AND (:legajo IS NULL OR LOWER(e.legajo) LIKE LOWER(CONCAT('%', :legajo, '%')))
          AND (:q IS NULL OR (
               LOWER(e.nombre) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(e.apellido) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(e.numeroDocumento) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(e.legajo) LIKE LOWER(CONCAT('%', :q, '%'))
          ))
        ORDER BY e.apellido ASC, e.nombre ASC
    """)
    Page<Empleado> buscarConFiltros(
            @Param("rol") RolUsuario rol,
            @Param("legajo") String legajo,
            @Param("q") String q,
            Pageable pageable);
}

