package org.example.repositorios;


import org.example.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepositorio extends JpaRepository<Usuario, String> {
    Optional<Usuario> findByMailAndAltaTrue(String mail);
    Optional<Usuario> findByIdAndAltaTrue(String id);
    boolean existsByMail(String mail);
    boolean existsByDni(long dni);
    List<Usuario> findByAltaTrue();
}
