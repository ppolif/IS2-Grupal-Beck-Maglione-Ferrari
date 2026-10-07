package com.example.persona.repositories;

import com.example.persona.entities.Autor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutorRepository extends BaseRepository<Autor, Long> {

    List<Autor> findByNombreContainingOrApellidoContaining(String nombre, String apellido);
    Page<Autor> findByNombreContainingOrApellidoContaining(String nombre, String apellido, Pageable pageable);

    @Query(value = "SELECT a FROM Autor a WHERE a.nombre LIKE %:filtro% OR a.apellido LIKE %:filtro%")
    List<Autor> search(@Param("filtro") String filtro);

    @Query(value = "SELECT a FROM Autor a WHERE a.nombre LIKE %:filtro% OR a.apellido LIKE %:filtro%")
    Page<Autor> search(@Param("filtro") String filtro, Pageable pageable);

    @Query(
            value = "SELECT * FROM autor WHERE autor.nombre LIKE CONCAT('%', :filtro, '%') OR autor.apellido LIKE CONCAT('%', :filtro, '%')",
            nativeQuery = true
    )
    List<Autor> searchNativo(@Param("filtro") String filtro);

    @Query(
            value = "SELECT * FROM autor WHERE autor.nombre LIKE CONCAT('%', :filtro, '%') OR autor.apellido LIKE CONCAT('%', :filtro, '%')",
            countQuery = "SELECT count(*) FROM autor WHERE autor.nombre LIKE CONCAT('%', :filtro, '%') OR autor.apellido LIKE CONCAT('%', :filtro, '%')",
            nativeQuery = true
    )
    Page<Autor> searchNativo(@Param("filtro") String filtro, Pageable pageable);
}
