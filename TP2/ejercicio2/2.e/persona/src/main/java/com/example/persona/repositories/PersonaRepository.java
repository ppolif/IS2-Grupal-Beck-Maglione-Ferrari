package com.example.persona.repositories;

import com.example.persona.entities.Persona;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PersonaRepository extends BaseRepository<Persona, Long> {

    List<Persona> findByNombreContainingOrApellidoContaining(String nombre, String apellido);
    Page<Persona> findByNombreContainingOrApellidoContaining(String nombre, String apellido, Pageable pageable);

    Optional<Persona> findByDni(int dni);
    boolean existsByDni(int dni);

    @Query(value = "SELECT p FROM Persona p WHERE p.nombre LIKE %:filtro% OR p.apellido LIKE %:filtro%")
    List<Persona> search(@Param("filtro") String filtro);

    @Query(value = "SELECT p FROM Persona p WHERE p.nombre LIKE %:filtro% OR p.apellido LIKE %:filtro%")
    Page<Persona> search(@Param("filtro") String filtro, Pageable pageable);

    @Query(
            value = "SELECT * FROM persona WHERE persona.nombre LIKE CONCAT('%', :filtro, '%') OR persona.apellido LIKE CONCAT('%', :filtro, '%')",
            nativeQuery = true
    )
    List<Persona> searchNativo(@Param("filtro") String filtro);

    @Query(
            value = "SELECT * FROM persona WHERE persona.nombre LIKE CONCAT('%', :filtro, '%') OR persona.apellido LIKE CONCAT('%', :filtro, '%')",
            countQuery = "SELECT count(*) FROM persona",
            nativeQuery = true
    )
    Page<Persona> searchNativo(@Param("filtro") String filtro, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Persona p LEFT JOIN FETCH p.libros")
    List<Persona> findAllConLibros();

    @Query("SELECT DISTINCT p FROM Persona p JOIN p.libros l WHERE l.fechaVencimientoDevolucion = :fecha")
    List<Persona> findPersonasConLibrosAVencer(@Param("fecha") LocalDate fecha);

    @Query("SELECT p FROM Persona p WHERE p.fechaNacimiento IS NOT NULL AND MONTH(p.fechaNacimiento) = :mes AND DAY(p.fechaNacimiento) = :dia")
    List<Persona> findPersonasPorCumpleanios(@Param("mes") int mes, @Param("dia") int dia);
}
