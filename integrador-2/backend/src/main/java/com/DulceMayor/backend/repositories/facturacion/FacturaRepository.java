package com.DulceMayor.backend.repositories.facturacion;

import com.DulceMayor.backend.entities.facturacion.EstadoFactura;
import com.DulceMayor.backend.entities.facturacion.Factura;
import com.DulceMayor.backend.repositories.BaseRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de acceso a datos para la entidad {@link Factura}.
 */
@Repository
public interface FacturaRepository extends BaseRepository<Factura> {

    Optional<Factura> findByPedidoIdAndEliminadoFalse(String pedidoId);

    Optional<Factura> findFirstByOrderByNumeroFacturaDesc();

    List<Factura> findAllByEliminadoFalseOrderByFechaFacturaDesc();

    List<Factura> findAllByEstadoAndEliminadoFalseOrderByFechaFacturaDesc(EstadoFactura estado);

    @Query("""
            SELECT f FROM Factura f
            WHERE f.eliminado = false
              AND (:estado IS NULL OR f.estado = :estado)
              AND (:desde IS NULL OR f.fechaFactura >= :desde)
              AND (:hasta IS NULL OR f.fechaFactura <= :hasta)
            ORDER BY f.fechaFactura DESC, f.numeroFactura DESC
            """)
    Page<Factura> buscarConFiltros(
            @Param("estado") EstadoFactura estado,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            Pageable pageable
    );
}

