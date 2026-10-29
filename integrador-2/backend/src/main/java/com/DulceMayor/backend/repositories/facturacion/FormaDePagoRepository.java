package com.DulceMayor.backend.repositories.facturacion;

import com.DulceMayor.backend.entities.facturacion.FormaDePago;
import com.DulceMayor.backend.entities.facturacion.TipoPago;
import com.DulceMayor.backend.repositories.BaseRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de acceso a datos para la entidad {@link FormaDePago}.
 */
@Repository
public interface FormaDePagoRepository extends BaseRepository<FormaDePago> {

    List<FormaDePago> findAllByEliminadoFalseOrderByTipoPagoAsc();

    List<FormaDePago> findAllByTipoPagoAndEliminadoFalse(TipoPago tipoPago);
}

