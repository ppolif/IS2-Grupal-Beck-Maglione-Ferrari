package com.example.persona.repositories;

import com.example.persona.entities.Persona;
import com.example.persona.entities.Prestamo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrestamoRepository extends BaseRepository<Prestamo, Long> {
    List<Prestamo> findByEstado(String estado);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Prestamo p WHERE p.persona.id = :personaId AND p.estado = :estado AND (p.activo = true OR p.activo IS NULL)")
    boolean existsByPersonaIdAndEstado(@Param("personaId") Long personaId, @Param("estado") String estado);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Prestamo p WHERE p.persona.id = :personaId AND p.estado = :estado AND p.id <> :prestamoId AND (p.activo = true OR p.activo IS NULL)")
    boolean existsByPersonaIdAndEstadoAndIdNot(@Param("personaId") Long personaId, @Param("estado") String estado, @Param("prestamoId") Long prestamoId);

    List<Prestamo> findByPersonaId(Long personaId);

    @Query("SELECT DISTINCT p FROM Persona p JOIN Prestamo pr ON pr.persona.id = p.id WHERE (p.activo = true OR p.activo IS NULL) AND (pr.activo = true OR pr.activo IS NULL)")
    List<Persona> findPersonasConHistorialPrestamos();

    @Query("SELECT pr FROM Prestamo pr WHERE pr.persona.id = :personaId AND (pr.activo = true OR pr.activo IS NULL) ORDER BY pr.fechaPrestamo DESC")
    List<Prestamo> findByPersonaIdOrderByFechaPrestamoDesc(@Param("personaId") Long personaId);
}
