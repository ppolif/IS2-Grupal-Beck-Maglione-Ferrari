package com.DulceMayor.backend.repositories;

import com.DulceMayor.backend.entities.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de acceso a datos (Patrón DAO) para la entidad Usuario.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    Optional<Usuario> findByNombreUsuarioAndEliminadoFalse(String nombreUsuario);

    Optional<Usuario> findByIdAndEliminadoFalse(String id);

    boolean existsByNombreUsuario(String nombreUsuario);
}

