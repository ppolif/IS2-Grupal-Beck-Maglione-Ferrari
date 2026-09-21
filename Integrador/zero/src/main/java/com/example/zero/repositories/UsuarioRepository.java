package com.example.zero.repositories;

import com.example.zero.entidades.persona.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    default Optional<Usuario> find(String id) {
        return id != null ? findById(id) : Optional.empty();
    }

    default Optional<Usuario> find(UUID id) {
        return id != null ? find(id.toString()) : Optional.empty();
    }

    @Query("SELECT u FROM Usuario u WHERE u.id = :id AND u.eliminado = false")
    Optional<Usuario> findActive(@Param("id") String id);

    default Optional<Usuario> findActive(UUID id) {
        return id != null ? findActive(id.toString()) : Optional.empty();
    }

    Optional<Usuario> findByNombreUsuarioAndEliminadoFalse(String nombreUsuario);

    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    List<Usuario> findByEliminadoFalse();
}

