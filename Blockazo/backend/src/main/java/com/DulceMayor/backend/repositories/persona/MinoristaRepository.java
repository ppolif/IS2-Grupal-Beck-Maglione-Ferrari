package com.DulceMayor.backend.repositories.persona;

import com.DulceMayor.backend.entities.persona.Minorista;
import com.DulceMayor.backend.repositories.BaseRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para la entidad {@link Minorista}.
 */
@Repository
public interface MinoristaRepository extends BaseRepository<Minorista> {

    Optional<Minorista> findByUsuarioIdAndEliminadoFalse(String usuarioId);

    Optional<Minorista> findByNumeroDocumentoAndEliminadoFalse(String numeroDocumento);

    @Query("""
        SELECT m FROM Minorista m
        WHERE m.eliminado = false
          AND m.usuario IS NOT NULL
          AND LOWER(m.usuario.nombreUsuario) = LOWER(:email)
    """)
    Optional<Minorista> findActivoByEmail(@Param("email") String email);

    @Query("""
        SELECT m FROM Minorista m
        WHERE m.eliminado = false
          AND (:numeroDocumento IS NULL OR m.numeroDocumento = :numeroDocumento)
          AND (:q IS NULL OR (
               LOWER(m.nombre) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(m.apellido) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(m.numeroDocumento) LIKE LOWER(CONCAT('%', :q, '%'))
          ))
        ORDER BY m.apellido ASC, m.nombre ASC
    """)
    Page<Minorista> buscarConFiltros(
            @Param("numeroDocumento") String numeroDocumento,
            @Param("q") String q,
            Pageable pageable);
}
