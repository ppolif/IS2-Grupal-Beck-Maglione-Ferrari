package com.DulceMayor.backend.repositories.pedido;

import com.DulceMayor.backend.entities.pedido.DetallePedido;
import com.DulceMayor.backend.repositories.BaseRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de acceso a datos para la entidad {@link DetallePedido}.
 */
@Repository
public interface DetallePedidoRepository extends BaseRepository<DetallePedido> {
}

