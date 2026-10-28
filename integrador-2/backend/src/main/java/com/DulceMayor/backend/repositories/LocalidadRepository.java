package com.DulceMayor.backend.repositories;

import com.DulceMayor.backend.entities.Localidad;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de acceso a datos para la entidad {@link Localidad}.
 */
@Repository
public interface LocalidadRepository extends BaseRepository<Localidad> {

    boolean existsByNombreIgnoreCaseAndDepartamentoIdAndEliminadoFalse(String nombre, String departamentoId);

    boolean existsByNombreIgnoreCaseAndDepartamentoIdAndIdNotAndEliminadoFalse(
            String nombre, String departamentoId, String id);

    Optional<Localidad> findByNombreIgnoreCaseAndDepartamentoIdAndEliminadoFalse(String nombre, String departamentoId);

    List<Localidad> findAllByEliminadoFalseOrderByNombreAsc();

    List<Localidad> findAllByDepartamentoIdAndEliminadoFalseOrderByNombreAsc(String departamentoId);
}

