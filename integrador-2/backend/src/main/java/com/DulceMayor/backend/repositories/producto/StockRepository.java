package com.DulceMayor.backend.repositories.producto;

import com.DulceMayor.backend.entities.producto.Stock;
import java.util.Optional;

import com.DulceMayor.backend.repositories.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de acceso a datos para la entidad {@link Stock}.
 * Gestiona la persistencia y consulta del histórico de movimientos de inventario.
 */
@Repository
public interface StockRepository extends BaseRepository<Stock> {

    /**
     * Obtiene el último movimiento de stock registrado en el sistema,
     * ordenado cronológicamente descendente por fecha y hora.
     */
    Optional<Stock> findFirstByEliminadoFalseOrderByFechaHoraDescIdDesc();

    /**
     * Búsqueda de stock por producto a través de DetalleFactura según el diseño UML
     * (Stock -> DetalleFactura -> Producto).
     */
    Optional<Stock> findFirstByDetalleFacturaProductoIdAndEliminadoFalseOrderByFechaHoraDescIdDesc(String productoId);

    /**
     * Consulta paginada del histórico de movimientos de stock, permitiendo filtrar
     * por condición de existencia (solo sin stock o con stock), ordenado de más reciente a más antiguo.
     */
    @Query("""
            SELECT s FROM Stock s
            WHERE s.eliminado = false
              AND (:soloSinStock IS NULL OR (:soloSinStock = true AND s.cantidadActual = 0) OR (:soloSinStock = false AND s.cantidadActual > 0))
            ORDER BY s.fechaHora DESC, s.id DESC
            """)
    Page<Stock> listarConFiltros(
            @Param("q") String q,
            @Param("soloSinStock") Boolean soloSinStock,
            Pageable pageable
    );
}
