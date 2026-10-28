package com.DulceMayor.backend.repositories;

import com.DulceMayor.backend.entities.Producto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de acceso a datos para la entidad {@link Producto}.
 */
@Repository
public interface ProductoRepository extends BaseRepository<Producto> {

    boolean existsByCodigoIgnoreCaseAndEliminadoFalse(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNotAndEliminadoFalse(String codigo, String id);

    Optional<Producto> findByCodigoIgnoreCaseAndEliminadoFalse(String codigo);

    Optional<Producto> findByNombreIgnoreCaseAndEliminadoFalse(String nombre);

    List<Producto> findAllByEliminadoFalseOrderByNombreAsc();

    List<Producto> findAllBySubCategoriaIdAndEliminadoFalseOrderByNombreAsc(String subCategoriaId);

    ///query para filtrar productos, de forma paginada para que no reviente la memoria lol
    /// poco optimizada la consulta con eso de buscar segun el stock pero bueno
    /// esto va a cambiar porque todavia no esta implementado detalleFactura
    @Query("""
            SELECT p FROM Producto p
            WHERE p.eliminado = false
              AND (:q IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(p.codigo) LIKE LOWER(CONCAT('%', :q, '%')))
              AND (:categoriaId IS NULL OR p.subCategoria.categoria.id = :categoriaId)
              AND (:subCategoriaId IS NULL OR p.subCategoria.id = :subCategoriaId)
              AND (:enOferta IS NULL OR p.enOferta = :enOferta)
              AND (
                   :soloConStock IS NULL OR :soloConStock = false
                   OR EXISTS (
                       SELECT s FROM Stock s
                       WHERE s.eliminado = false
                         AND s.cantidadActual > 0
                   )
              )
            """)
    Page<Producto> buscarConFiltros(
            @Param("q") String q,
            @Param("categoriaId") String categoriaId,
            @Param("subCategoriaId") String subCategoriaId,
            @Param("enOferta") Boolean enOferta,
            @Param("soloConStock") Boolean soloConStock,
            Pageable pageable
    );
}
