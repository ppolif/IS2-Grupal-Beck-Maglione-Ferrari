package org.example.repositorios;

import org.example.entidades.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AutorRepositorio extends JpaRepository<Autor, String> {

    List<Autor> findByAltaTrue();

    Optional<Autor> findByIdAndAltaTrue(String id);
}