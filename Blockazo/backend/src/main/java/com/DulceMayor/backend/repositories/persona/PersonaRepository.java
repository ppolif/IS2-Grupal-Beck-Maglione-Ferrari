package com.DulceMayor.backend.repositories.persona;

import com.DulceMayor.backend.entities.persona.Persona;
import com.DulceMayor.backend.entities.persona.TipoDocumento;
import com.DulceMayor.backend.repositories.BaseRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para la entidad abstracta {@link Persona}.
 */
@Repository
public interface PersonaRepository extends BaseRepository<Persona> {

    Optional<Persona> findByUsuarioIdAndEliminadoFalse(String usuarioId);

    boolean existsByTipoDocumentoAndNumeroDocumentoAndEliminadoFalse(
            TipoDocumento tipoDocumento, String numeroDocumento);

    boolean existsByTipoDocumentoAndNumeroDocumentoAndIdNotAndEliminadoFalse(
            TipoDocumento tipoDocumento, String numeroDocumento, String id);
}

