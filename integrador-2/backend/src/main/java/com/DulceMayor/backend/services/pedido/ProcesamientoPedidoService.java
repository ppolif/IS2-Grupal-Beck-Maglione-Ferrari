package com.DulceMayor.backend.services.pedido;

import com.DulceMayor.backend.config.ConflictoException;
import com.DulceMayor.backend.config.RecursoNoEncontradoException;
import com.DulceMayor.backend.dtos.facturacion.FacturaResponse;
import com.DulceMayor.backend.dtos.pedido.ItemSinStock;
import com.DulceMayor.backend.dtos.pedido.PedidoResponse;
import com.DulceMayor.backend.dtos.pedido.ProcesamientoPedidoResponse;
import com.DulceMayor.backend.dtos.producto.StockResponse;
import com.DulceMayor.backend.entities.pedido.DetallePedido;
import com.DulceMayor.backend.entities.pedido.EstadoPedido;
import com.DulceMayor.backend.entities.pedido.Pedido;
import com.DulceMayor.backend.services.facturacion.FacturaService;
import com.DulceMayor.backend.services.producto.StockService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio orquestador para el procesamiento transaccional de un {@link Pedido}
 * conforme a {@code 03-arquitectura.md} (§5), {@code 02-reglas-negocio.md} (§1.1) y
 * {@code GENERAL.md} (§10.1 regla 5).
 * <p>
 * Coordina {@link PedidoService}, {@link StockService} y {@link FacturaService} en una única
 * transacción sin inyectar repositorios ni generar dependencias cruzadas entre los servicios individuales.
 * </p>
 */
@Service
public class ProcesamientoPedidoService {

    private final PedidoService pedidoService;
    private final StockService stockService;
    private final FacturaService facturaService;

    public ProcesamientoPedidoService(
            PedidoService pedidoService,
            StockService stockService,
            FacturaService facturaService) {
        this.pedidoService = pedidoService;
        this.stockService = stockService;
        this.facturaService = facturaService;
    }

    /**
     * Verifica atómicamente el stock de todos los ítems de un pedido en estado {@code CARGADO} o {@code SIN_STOCK}.
     * <ul>
     *   <li>Si todos los ítems tienen stock suficiente: genera la {@code Factura} en {@code PENDIENTE_PAGO},
     *       descuenta el stock vinculándolo a cada {@code DetalleFactura} y transiciona el pedido a {@code FACTURADO}.</li>
     *   <li>Si falta stock en uno o más ítems: no descuenta stock ni genera factura, pasa el pedido a
     *       {@code SIN_STOCK} y detalla los faltantes en {@code itemsSinStock}.</li>
     * </ul>
     */
    @Transactional
    public ProcesamientoPedidoResponse procesarPedido(String pedidoId) {
        Pedido pedido = pedidoService.obtenerEntidad(pedidoId);

        if (pedido.getEstado() != EstadoPedido.CARGADO && pedido.getEstado() != EstadoPedido.SIN_STOCK) {
            throw new ConflictoException(
                    "TRANSICION_INVALIDA",
                    "Solo se pueden procesar pedidos en estado CARGADO o SIN_STOCK. Estado actual: " + pedido.getEstado()
            );
        }

        // Agrupa cantidades requeridas por producto para contemplar pedidos con múltiples líneas del mismo artículo
        Map<String, AcumuladoProducto> requeridosPorProducto = new LinkedHashMap<>();
        for (DetallePedido detalle : pedido.getDetalles()) {
            String prodId = detalle.getProducto().getId();
            String prodNombre = detalle.getProducto().getNombre();
            requeridosPorProducto.compute(prodId, (k, actual) -> actual == null
                    ? new AcumuladoProducto(prodId, prodNombre, detalle.getCantidad())
                    : new AcumuladoProducto(prodId, prodNombre, actual.solicitado() + detalle.getCantidad()));
        }

        List<ItemSinStock> itemsSinStock = new ArrayList<>();
        for (AcumuladoProducto req : requeridosPorProducto.values()) {
            int disponible = consultarDisponible(req.productoId());
            if (disponible < req.solicitado()) {
                itemsSinStock.add(new ItemSinStock(
                        req.productoId(),
                        req.productoNombre(),
                        req.solicitado(),
                        disponible
                ));
            }
        }

        if (!itemsSinStock.isEmpty()) {
            StringBuilder motivo = new StringBuilder("Stock insuficiente para: ");
            for (int i = 0; i < itemsSinStock.size(); i++) {
                ItemSinStock faltante = itemsSinStock.get(i);
                if (i > 0) {
                    motivo.append(", ");
                }
                motivo.append(faltante.productoNombre())
                        .append(" (solicitado: ")
                        .append(faltante.solicitado())
                        .append(", disponible: ")
                        .append(faltante.disponible())
                        .append(")");
            }
            PedidoResponse pedidoRetenido = pedidoService.marcarSinStock(pedido, motivo.toString());
            return new ProcesamientoPedidoResponse(pedidoRetenido, null, itemsSinStock);
        }

        FacturaResponse factura = facturaService.generarDesdePedido(pedido);
        PedidoResponse pedidoFacturado = pedidoService.marcarFacturado(
                pedido,
                "Pedido facturado - Factura Nro. " + factura.numeroFactura()
        );

        return new ProcesamientoPedidoResponse(pedidoFacturado, factura, List.of());
    }

    private int consultarDisponible(String productoId) {
        try {
            StockResponse stock = stockService.buscarPorProducto(productoId);
            return stock != null && stock.cantidadActual() != null ? stock.cantidadActual() : 0;
        } catch (RecursoNoEncontradoException ex) {
            return 0;
        }
    }

    private record AcumuladoProducto(String productoId, String productoNombre, int solicitado) {
    }
}

