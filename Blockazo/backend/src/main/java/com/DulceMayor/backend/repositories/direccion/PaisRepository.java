package com.DulceMayor.backend.repositories.direccion;

import com.DulceMayor.backend.entities.direccion.Pais;
import java.util.List;
import java.util.Optional;

import com.DulceMayor.backend.repositories.BaseRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de acceso a datos para la entidad {@link Pais}.
 */
@Repository
public interface PaisRepository extends BaseRepository<Pais> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, String id);

    boolean existsByNombreIgnoreCaseAndEliminadoFalse(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNotAndEliminadoFalse(String nombre, String id);

    Optional<Pais> findByNombreIgnoreCaseAndEliminadoFalse(String nombre);

    List<Pais> findAllByEliminadoFalseOrderByNombreAsc();
}

