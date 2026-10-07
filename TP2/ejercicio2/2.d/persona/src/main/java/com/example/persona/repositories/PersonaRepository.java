package com.example.persona.repositories;

import com.example.persona.entities.Persona;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonaRepository extends BaseRepository<Persona, Long> {

    List<Persona> findByNombreContainingOrApellidoContaining(String nombre, String apellido);
    Page<Persona> findByNombreContainingOrApellidoContaining(String nombre, String apellido, Pageable pageable);

    //boolean existsByDni(int dni);

    @Query(value = "SELECT p FROM Persona p WHERE (p.nombre LIKE %:filtro% OR p.apellido LIKE %:filtro%) AND (p.activo = true OR p.activo IS NULL)")
    List<Persona> search(@Param("filtro") String filtro);

    @Query(value = "SELECT p FROM Persona p WHERE (p.nombre LIKE %:filtro% OR p.apellido LIKE %:filtro%) AND (p.activo = true OR p.activo IS NULL)")
    Page<Persona> search(@Param("filtro") String filtro, Pageable pageable);

    @Query(
            value = "SELECT * FROM persona WHERE (persona.nombre LIKE CONCAT('%', :filtro, '%') OR persona.apellido LIKE CONCAT('%', :filtro, '%')) AND (persona.activo IS NULL OR persona.activo = 1)",
            nativeQuery = true
    )
    List<Persona> searchNativo(@Param("filtro") String filtro);

    @Query(
            value = "SELECT * FROM persona WHERE (persona.nombre LIKE CONCAT('%', :filtro, '%') OR persona.apellido LIKE CONCAT('%', :filtro, '%')) AND (persona.activo IS NULL OR persona.activo = 1)",
            countQuery = "SELECT count(*) FROM persona WHERE (persona.nombre LIKE CONCAT('%', :filtro, '%') OR persona.apellido LIKE CONCAT('%', :filtro, '%')) AND (persona.activo IS NULL OR persona.activo = 1)",
            nativeQuery = true
    )
    Page<Persona> searchNativo(@Param("filtro") String filtro, Pageable pageable);
}
