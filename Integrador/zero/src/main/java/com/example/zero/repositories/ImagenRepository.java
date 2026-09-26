package com.example.zero.repositories;

import com.example.zero.entidades.Imagen;
import com.example.zero.enums.TipoImagen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImagenRepository extends JpaRepository<Imagen, String> {

    @Query("SELECT i FROM Imagen i WHERE i.id = :id AND i.eliminado = false")
    Optional<Imagen> findActive(@Param("id") String id);

    Optional<Imagen> findByIdAndEliminadoFalse(String id);

    List<Imagen> findByTipoImagenAndEliminadoFalse(TipoImagen tipoImagen);

    List<Imagen> findByEliminadoFalse();
}
