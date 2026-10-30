package com.DulceMayor.backend.repositories.pedido;

import com.DulceMayor.backend.entities.pedido.HistorialEstadoPedido;
import com.DulceMayor.backend.repositories.BaseRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de acceso a datos para la trazabilidad {@link HistorialEstadoPedido}.
 */
@Repository
public interface HistorialEstadoPedidoRepository extends BaseRepository<HistorialEstadoPedido> {

    List<HistorialEstadoPedido> findAllByPedidoIdAndEliminadoFalseOrderByFechaHoraAscIdAsc(String pedidoId);
}

