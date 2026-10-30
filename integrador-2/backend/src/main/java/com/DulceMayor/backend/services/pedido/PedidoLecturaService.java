package com.DulceMayor.backend.services.pedido;

import com.DulceMayor.backend.dtos.pedido.PedidoMapper;
import com.DulceMayor.backend.dtos.pedido.PedidoMiCuentaPage;
import com.DulceMayor.backend.dtos.pedido.PedidoPage;
import com.DulceMayor.backend.dtos.pedido.PedidoResponse;
import com.DulceMayor.backend.entities.facturacion.Factura;
import com.DulceMayor.backend.entities.pedido.EstadoPedido;
import com.DulceMayor.backend.entities.pedido.Pedido;
import com.DulceMayor.backend.repositories.facturacion.FacturaRepository;
import com.DulceMayor.backend.repositories.pedido.PedidoRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de solo lectura para proyectar pedidos resolviendo {@code facturaId} desde {@link FacturaRepository}
 * y ejecutar consultas paginadas.
 * <p>
 * Conforme a {@code GENERAL.md} (§7 y §10.1 regla 6), {@link Pedido} no referencia a {@link Factura} y
 * {@code PedidoService} no depende de {@code FacturaService}; las clases {@code *LecturaService} pueden
 * consultar ambos repositorios en modo solo lectura sin generar acoplamiento circular entre agregados.
 * </p>
 *
 * <p><b>Patrón GoF aplicado: DTO / Adapter (Estructural)</b></p>
 * Ensambla {@link PedidoResponse} enriqueciendo la entidad {@link Pedido} con la referencia calculada
 * de su {@link Factura} activa si fue facturado.
 */
@Service
@Transactional(readOnly = true)
public class PedidoLecturaService {

    private final PedidoRepository pedidoRepository;
    private final FacturaRepository facturaRepository;
    private final PedidoMapper pedidoMapper;

    public PedidoLecturaService(
            PedidoRepository pedidoRepository,
            FacturaRepository facturaRepository,
            PedidoMapper pedidoMapper) {
        this.pedidoRepository = pedidoRepository;
        this.facturaRepository = facturaRepository;
        this.pedidoMapper = pedidoMapper;
    }

    /**
     * Proyecta un {@link Pedido} a {@link PedidoResponse} resolviendo {@code facturaId} si posee factura activa.
     */
    public PedidoResponse enriquecerResponse(Pedido pedido) {
        PedidoResponse base = pedidoMapper.aResponse(pedido);
        String facturaId = pedido.getId() != null
                ? facturaRepository.findByPedidoIdAndEliminadoFalse(pedido.getId())
                        .map(Factura::getId)
                        .orElse(null)
                : null;

        return new PedidoResponse(
                base.id(),
                base.numeroPedido(),
                base.fechaHoraCarga(),
                base.estado(),
                base.puntoDeVentaId(),
                base.preventorId(),
                base.distribuidorId(),
                base.codigoPromocion(),
                base.subtotal(),
                base.descuento(),
                base.total(),
                base.detalles() != null ? base.detalles() : List.of(),
                facturaId
        );
    }

    /**
     * Consulta paginada de pedidos con filtros opcionales de estado, preventor, distribuidor, punto de venta y fechas.
     */
    public PedidoPage listarPaginado(
            EstadoPedido estado,
            String preventorId,
            String distribuidorId,
            String puntoDeVentaId,
            LocalDate desde,
            LocalDate hasta,
            Pageable pageable) {

        LocalDateTime fechaDesde = desde != null ? desde.atStartOfDay() : null;
        LocalDateTime fechaHasta = hasta != null ? hasta.atTime(LocalTime.MAX) : null;

        Page<Pedido> pagina = pedidoRepository.buscarConFiltros(
                estado,
                limpiarFiltro(preventorId),
                limpiarFiltro(distribuidorId),
                limpiarFiltro(puntoDeVentaId),
                fechaDesde,
                fechaHasta,
                pageable
        );

        List<PedidoResponse> contenido = new ArrayList<>();
        for (Pedido p : pagina.getContent()) {
            contenido.add(enriquecerResponse(p));
        }

        return new PedidoPage(
                contenido,
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages()
        );
    }

    /**
     * Consulta paginada de pedidos para el portal "Mi cuenta" del cliente minorista.
     */
    public PedidoMiCuentaPage listarMiCuenta(EstadoPedido estado, Pageable pageable) {
        Page<Pedido> pagina = pedidoRepository.buscarConFiltros(
                estado,
                null,
                null,
                null,
                null,
                null,
                pageable
        );

        List<PedidoResponse> contenido = new ArrayList<>();
        for (Pedido p : pagina.getContent()) {
            contenido.add(enriquecerResponse(p));
        }

        return new PedidoMiCuentaPage(
                contenido,
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages()
        );
    }

    private String limpiarFiltro(String valor) {
        return (valor != null && !valor.isBlank()) ? valor.trim() : null;
    }
}

