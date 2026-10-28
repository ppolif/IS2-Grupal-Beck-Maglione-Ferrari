package com.DulceMayor.backend.repositories;

import com.DulceMayor.backend.entities.SubCategoria;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de acceso a datos para la entidad {@link SubCategoria}.
 */
@Repository
public interface SubCategoriaRepository extends BaseRepository<SubCategoria> {

    List<SubCategoria> findAllByEliminadoFalseOrderByNombreAsc();

    List<SubCategoria> findAllByCategoriaIdAndEliminadoFalseOrderByNombreAsc(String categoriaId);

    boolean existsByNombreIgnoreCaseAndCategoriaIdAndEliminadoFalse(String nombre, String categoriaId);

    boolean existsByNombreIgnoreCaseAndCategoriaIdAndIdNotAndEliminadoFalse(String nombre, String categoriaId, String id);

    boolean existsByCategoriaIdAndEliminadoFalse(String categoriaId);

    Optional<SubCategoria> findByNombreIgnoreCaseAndCategoriaIdAndEliminadoFalse(String nombre, String categoriaId);
}

