package com.DulceMayor.backend.services.facturacion;

import com.DulceMayor.backend.dtos.facturacion.FacturaMapper;
import com.DulceMayor.backend.dtos.facturacion.FacturaMiCuentaPage;
import com.DulceMayor.backend.dtos.facturacion.FacturaPage;
import com.DulceMayor.backend.dtos.facturacion.FacturaResponse;
import com.DulceMayor.backend.dtos.facturacion.PagoMapper;
import com.DulceMayor.backend.dtos.facturacion.PagoResponse;
import com.DulceMayor.backend.entities.facturacion.EstadoFactura;
import com.DulceMayor.backend.entities.facturacion.Factura;
import com.DulceMayor.backend.entities.facturacion.Pago;
import com.DulceMayor.backend.repositories.facturacion.FacturaRepository;
import com.DulceMayor.backend.repositories.facturacion.PagoRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de solo lectura para la proyección y consulta de facturas con su estado de pagos.
 * <p>
 * Conforme a la sección 10.1 (regla 6) de {@code GENERAL.md} y la exención en {@code ArquitecturaTests},
 * las clases {@code *LecturaService} son de solo lectura ({@code @Transactional(readOnly = true)}) y
 * pueden consultar múltiples repositorios ({@link FacturaRepository} y {@link PagoRepository}) para
 * ensamblar proyecciones con {@code totalPagado}, {@code saldo}, {@code detalles} y {@code pagos}.
 * </p>
 *
 * <p><b>Patrón GoF aplicado: DTO / Adapter (Estructural)</b></p>
 * Ensambla un {@link FacturaResponse} enriquecido combinando la entidad {@link Factura} con la suma
 * y el listado de sus {@link Pago} activos sin acoplar {@link FacturaService} con {@link PagoRepository}.
 */
@Service
@Transactional(readOnly = true)
public class FacturaLecturaService {

    private final FacturaRepository facturaRepository;
    private final PagoRepository pagoRepository;
    private final FacturaMapper facturaMapper;
    private final PagoMapper pagoMapper;

    public FacturaLecturaService(
            FacturaRepository facturaRepository,
            PagoRepository pagoRepository,
            FacturaMapper facturaMapper,
            PagoMapper pagoMapper) {
        this.facturaRepository = facturaRepository;
        this.pagoRepository = pagoRepository;
        this.facturaMapper = facturaMapper;
        this.pagoMapper = pagoMapper;
    }

    /**
     * Calcula la suma total de los pagos activos registrados para una factura.
     */
    public double obtenerTotalPagado(String facturaId) {
        return Optional.ofNullable(pagoRepository.sumarPagosActivosPorFactura(facturaId))
                .orElse(0.0);
    }

    /**
     * Verifica si una factura posee al menos un pago activo (no eliminado).
     */
    public boolean tienePagosActivos(String facturaId) {
        return pagoRepository.existsByFacturaIdAndEliminadoFalse(facturaId);
    }

    /**
     * Lista los pagos activos de una factura ordenados cronológicamente.
     */
    public List<PagoResponse> listarPagosPorFactura(String facturaId) {
        List<Pago> pagos = pagoRepository.findAllByFacturaIdAndEliminadoFalseOrderByFechaPagoAsc(facturaId);
        return pagoMapper.aResponseList(pagos);
    }

    /**
     * Enriquecer el DTO {@link FacturaResponse} con el cálculo en tiempo real de {@code totalPagado},
     * {@code saldo = total - totalPagado} y la colección de {@code pagos} activos.
     */
    public FacturaResponse enriquecerResponse(Factura factura) {
        FacturaResponse base = facturaMapper.aResponse(factura);
        double totalFactura = factura.getTotal();
        double totalPagado = factura.getId() != null ? obtenerTotalPagado(factura.getId()) : 0.0;
        double saldo = Math.max(0.0, totalFactura - totalPagado);
        List<PagoResponse> pagos = factura.getId() != null
                ? listarPagosPorFactura(factura.getId())
                : List.of();

        return new FacturaResponse(
                base.id(),
                base.numeroFactura(),
                base.fechaFactura(),
                base.pedidoId(),
                base.estado(),
                totalFactura,
                totalPagado,
                saldo,
                base.detalles() != null ? base.detalles() : List.of(),
                pagos
        );
    }

    /**
     * Listado paginado de facturas con filtros opcionales de estado y rango de fechas.
     */
    public FacturaPage listarPaginado(
            EstadoFactura estado,
            String minoristaId,
            LocalDate desde,
            LocalDate hasta,
            Pageable pageable) {

        LocalDateTime fechaDesde = desde != null ? desde.atStartOfDay() : null;
        LocalDateTime fechaHasta = hasta != null ? hasta.atTime(LocalTime.MAX) : null;

        Page<Factura> pagina = facturaRepository.buscarConFiltros(
                estado,
                minoristaId,
                fechaDesde,
                fechaHasta,
                pageable
        );

        List<FacturaResponse> contenido = new ArrayList<>();
        for (Factura f : pagina.getContent()) {
            contenido.add(enriquecerResponse(f));
        }

        return new FacturaPage(
                contenido,
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages()
        );
    }

    /**
     * Listado paginado de facturas para la sección "Mi cuenta" del cliente minorista.
     */
    public FacturaMiCuentaPage listarMiCuenta(EstadoFactura estado, String minoristaId, Pageable pageable) {
        Page<Factura> pagina = facturaRepository.buscarConFiltros(
                estado,
                minoristaId,
                null,
                null,
                pageable
        );

        List<FacturaResponse> contenido = new ArrayList<>();
        for (Factura f : pagina.getContent()) {
            contenido.add(enriquecerResponse(f));
        }

        return new FacturaMiCuentaPage(
                contenido,
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages()
        );
    }
}
