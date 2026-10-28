package com.DulceMayor.backend.repositories;

import com.DulceMayor.backend.entities.Imagen;
import com.DulceMayor.backend.entities.TipoImagen;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de acceso a datos para la entidad {@link Imagen}.
 */
@Repository
public interface ImagenRepository extends BaseRepository<Imagen> {

    List<Imagen> findAllByTipoImagenAndEliminadoFalse(TipoImagen tipoImagen);
}

