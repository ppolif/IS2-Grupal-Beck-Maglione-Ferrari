package com.example.persona.repositories;

import com.example.persona.entities.Libro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LibroRepository extends BaseRepository<Libro, Long> {

    @Query(value = "SELECT l FROM Libro l WHERE l.titulo LIKE %:filtro% OR l.genero LIKE %:filtro%")
    List<Libro> search(@Param("filtro") String filtro);

    @Query(value = "SELECT l FROM Libro l WHERE l.titulo LIKE %:filtro% OR l.genero LIKE %:filtro%")
    Page<Libro> search(@Param("filtro") String filtro, Pageable pageable);
}
