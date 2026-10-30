package com.DulceMayor.backend.repositories.mayorista;

import com.DulceMayor.backend.entities.mayorista.ConfiguracionCorreoMayorista;
import com.DulceMayor.backend.repositories.BaseRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para la entidad {@link ConfiguracionCorreoMayorista}.
 */
@Repository
public interface ConfiguracionCorreoMayoristaRepository extends BaseRepository<ConfiguracionCorreoMayorista> {

    Optional<ConfiguracionCorreoMayorista> findByMayoristaIdAndEliminadoFalse(String mayoristaId);

    boolean existsByMayoristaIdAndEliminadoFalse(String mayoristaId);
}

