package com.DulceMayor.backend.repositories;

import com.DulceMayor.backend.entities.EntidadBase;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Repositorio base genérico con soporte estándar para borrado lógico (eliminado = false).
 *
 * @param <E> Tipo de la entidad que extiende EntidadBase.
 */
@NoRepositoryBean
public interface BaseRepository<E extends EntidadBase> extends JpaRepository<E, String> {

    Optional<E> findByIdAndEliminadoFalse(String id);

    List<E> findAllByEliminadoFalse();

    boolean existsByIdAndEliminadoFalse(String id);
}

