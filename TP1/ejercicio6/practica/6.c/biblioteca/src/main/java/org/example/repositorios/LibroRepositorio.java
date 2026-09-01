package org.example.repositorios;

import org.example.entidades.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LibroRepositorio extends JpaRepository<Libro, Long> {

    List<Libro> findByAltaTrue();

    Optional<Libro> findByIsbnAndAltaTrue(Long isbn);
}