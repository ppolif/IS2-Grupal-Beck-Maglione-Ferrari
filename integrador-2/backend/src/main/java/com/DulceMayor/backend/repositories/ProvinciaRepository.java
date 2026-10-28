package com.DulceMayor.backend.repositories;

import com.DulceMayor.backend.entities.Provincia;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de acceso a datos para la entidad {@link Provincia}.
 */
@Repository
public interface ProvinciaRepository extends BaseRepository<Provincia> {

    boolean existsByNombreIgnoreCaseAndPaisIdAndEliminadoFalse(String nombre, String paisId);

    boolean existsByNombreIgnoreCaseAndPaisIdAndIdNotAndEliminadoFalse(String nombre, String paisId, String id);

    Optional<Provincia> findByNombreIgnoreCaseAndPaisIdAndEliminadoFalse(String nombre, String paisId);

    List<Provincia> findAllByEliminadoFalseOrderByNombreAsc();

    List<Provincia> findAllByPaisIdAndEliminadoFalseOrderByNombreAsc(String paisId);
}

