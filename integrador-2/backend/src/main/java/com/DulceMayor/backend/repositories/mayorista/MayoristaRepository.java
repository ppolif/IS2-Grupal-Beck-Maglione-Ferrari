package com.DulceMayor.backend.repositories.mayorista;

import com.DulceMayor.backend.entities.mayorista.Mayorista;
import com.DulceMayor.backend.entities.mayorista.TipoEmpresa;
import com.DulceMayor.backend.repositories.BaseRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para la entidad {@link Mayorista}.
 */
@Repository
public interface MayoristaRepository extends BaseRepository<Mayorista> {

    boolean existsByCuitAndEliminadoFalse(String cuit);

    boolean existsByCuitAndIdNotAndEliminadoFalse(String cuit, String id);

    List<Mayorista> findAllByEliminadoFalseOrderByTipoEmpresaAscRazonSocialAsc();

    List<Mayorista> findAllByTipoEmpresaAndEliminadoFalse(TipoEmpresa tipoEmpresa);
}

