package com.DulceMayor.backend.repositories.direccion;

import com.DulceMayor.backend.entities.direccion.Departamento;
import java.util.List;
import java.util.Optional;

import com.DulceMayor.backend.repositories.BaseRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de acceso a datos para la entidad {@link Departamento}.
 */
@Repository
public interface DepartamentoRepository extends BaseRepository<Departamento> {

    boolean existsByNombreIgnoreCaseAndProvinciaIdAndEliminadoFalse(String nombre, String provinciaId);

    boolean existsByNombreIgnoreCaseAndProvinciaIdAndIdNotAndEliminadoFalse(String nombre, String provinciaId, String id);

    Optional<Departamento> findByNombreIgnoreCaseAndProvinciaIdAndEliminadoFalse(String nombre, String provinciaId);

    List<Departamento> findAllByEliminadoFalseOrderByNombreAsc();

    List<Departamento> findAllByProvinciaIdAndEliminadoFalseOrderByNombreAsc(String provinciaId);
}

