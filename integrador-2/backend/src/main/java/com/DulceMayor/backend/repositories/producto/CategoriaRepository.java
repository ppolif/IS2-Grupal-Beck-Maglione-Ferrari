package com.DulceMayor.backend.repositories.producto;

import com.DulceMayor.backend.entities.producto.Categoria;
import java.util.List;
import java.util.Optional;

import com.DulceMayor.backend.repositories.BaseRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de acceso a datos para la entidad {@link Categoria}.
 * Incluye consultas de unicidad sin distinguir mayúsculas/minúsculas y filtrado de borrado lógico.
 */
@Repository
public interface CategoriaRepository extends BaseRepository<Categoria> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, String id);

    boolean existsByNombreIgnoreCaseAndEliminadoFalse(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNotAndEliminadoFalse(String nombre, String id);

    Optional<Categoria> findByNombreIgnoreCaseAndEliminadoFalse(String nombre);

    List<Categoria> findAllByEliminadoFalseOrderByNombreAsc();
}

