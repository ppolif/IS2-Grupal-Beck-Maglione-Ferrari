package com.DulceMayor.backend.repositories.facturacion;

import com.DulceMayor.backend.entities.facturacion.DetalleFactura;
import com.DulceMayor.backend.repositories.BaseRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de acceso a datos para la entidad {@link DetalleFactura}.
 */
@Repository
public interface DetalleFacturaRepository extends BaseRepository<DetalleFactura> {

    List<DetalleFactura> findAllByProductoIdAndEliminadoFalse(String productoId);
}

