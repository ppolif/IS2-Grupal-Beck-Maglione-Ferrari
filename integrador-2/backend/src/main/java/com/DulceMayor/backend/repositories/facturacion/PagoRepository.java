package com.DulceMayor.backend.repositories.facturacion;

import com.DulceMayor.backend.entities.facturacion.Pago;
import com.DulceMayor.backend.repositories.BaseRepository;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de acceso a datos para la entidad {@link Pago}.
 */
@Repository
public interface PagoRepository extends BaseRepository<Pago> {

    List<Pago> findAllByFacturaIdAndEliminadoFalseOrderByFechaPagoAsc(String facturaId);

    boolean existsByFacturaIdAndEliminadoFalse(String facturaId);

    @Query("""
            SELECT COALESCE(SUM(p.monto), 0.0)
            FROM Pago p
            WHERE p.factura.id = :facturaId
              AND p.eliminado = false
            """)
    Double sumarPagosActivosPorFactura(@Param("facturaId") String facturaId);
}

