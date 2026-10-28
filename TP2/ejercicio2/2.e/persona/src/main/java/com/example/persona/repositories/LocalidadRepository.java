package com.example.persona.repositories;

import com.example.persona.entities.Localidad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocalidadRepository extends BaseRepository<Localidad, Long> {

    List<Localidad> findByDenominacionContaining(String denominacion);
    Page<Localidad> findByDenominacionContaining(String denominacion, Pageable pageable);

    @Query(value = "SELECT l FROM Localidad l WHERE l.denominacion LIKE %:filtro% AND (l.activo = true OR l.activo IS NULL)")
    List<Localidad> search(@Param("filtro") String filtro);

    @Query(value = "SELECT l FROM Localidad l WHERE l.denominacion LIKE %:filtro% AND (l.activo = true OR l.activo IS NULL)")
    Page<Localidad> search(@Param("filtro") String filtro, Pageable pageable);

    @Query(
            value = "SELECT * FROM localidad WHERE localidad.denominacion LIKE CONCAT('%', :filtro, '%') AND (localidad.activo IS NULL OR localidad.activo = 1)",
            nativeQuery = true
    )
    List<Localidad> searchNativo(@Param("filtro") String filtro);

    @Query(
            value = "SELECT * FROM localidad WHERE localidad.denominacion LIKE CONCAT('%', :filtro, '%') AND (localidad.activo IS NULL OR localidad.activo = 1)",
            countQuery = "SELECT count(*) FROM localidad WHERE (localidad.activo IS NULL OR localidad.activo = 1)",
            nativeQuery = true
    )
    Page<Localidad> searchNativo(@Param("filtro") String filtro, Pageable pageable);
}
