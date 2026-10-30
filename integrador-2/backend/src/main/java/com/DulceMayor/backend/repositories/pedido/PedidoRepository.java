package com.DulceMayor.backend.repositories.pedido;

import com.DulceMayor.backend.entities.pedido.EstadoPedido;
import com.DulceMayor.backend.entities.pedido.Pedido;
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
 * Repositorio de acceso a datos para la entidad {@link Pedido}.
 */
@Repository
public interface PedidoRepository extends BaseRepository<Pedido> {

    Optional<Pedido> findFirstByOrderByNumeroPedidoDesc();

    List<Pedido> findAllByEliminadoFalseOrderByFechaHoraCargaDesc();

    @Query("""
            SELECT p FROM Pedido p
            JOIN p.puntoDeVenta pdv
            LEFT JOIN p.preventor pr
            LEFT JOIN p.distribuidor d
            WHERE p.eliminado = false
              AND (:estado IS NULL OR p.estado = :estado)
              AND (:preventorId IS NULL OR (pr IS NOT NULL AND pr.id = :preventorId))
              AND (:distribuidorId IS NULL OR (d IS NOT NULL AND d.id = :distribuidorId))
              AND (:puntoDeVentaId IS NULL OR pdv.id = :puntoDeVentaId)
              AND (:minoristaId IS NULL OR pdv.minorista.id = :minoristaId)
              AND (:desde IS NULL OR p.fechaHoraCarga >= :desde)
              AND (:hasta IS NULL OR p.fechaHoraCarga <= :hasta)
            ORDER BY p.fechaHoraCarga DESC, p.numeroPedido DESC
            """)
    Page<Pedido> buscarConFiltros(
            @Param("estado") EstadoPedido estado,
            @Param("preventorId") String preventorId,
            @Param("distribuidorId") String distribuidorId,
            @Param("puntoDeVentaId") String puntoDeVentaId,
            @Param("minoristaId") String minoristaId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            Pageable pageable
    );
}
