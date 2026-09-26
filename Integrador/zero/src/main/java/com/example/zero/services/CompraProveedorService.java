package com.example.zero.services;

import com.example.zero.entidades.compra.Detalle;
import com.example.zero.entidades.compra.FormaDePago;
import com.example.zero.entidades.compra.Stock;
import com.example.zero.entidades.compraProveedor.FacturaProveedor;
import com.example.zero.entidades.compraProveedor.Proveedor;
import com.example.zero.entidades.producto.Producto;
import com.example.zero.enums.EstadoFactura;
import com.example.zero.enums.TipoDePago;
import com.example.zero.repositories.*;
import com.example.zero.services.producto.ProductoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Servicio de negocio para la gestión de compras y órdenes a Proveedores (FacturaProveedor).
 */
@Service
public class CompraProveedorService {

    private final FacturaProveedorRepository facturaProveedorRepository;
    private final FacturaRepository facturaRepository;
    private final DetalleRepository detalleRepository;
    private final StockRepository stockRepository;
    private final ProveedorRepository proveedorRepository;
    private final ProductoRepository productoRepository;
    private final FormaDePagoRepository formaDePagoRepository;
    private final ProductoService productoService;

    public CompraProveedorService(FacturaProveedorRepository facturaProveedorRepository,
                                  FacturaRepository facturaRepository,
                                  DetalleRepository detalleRepository,
                                  StockRepository stockRepository,
                                  ProveedorRepository proveedorRepository,
                                  ProductoRepository productoRepository,
                                  FormaDePagoRepository formaDePagoRepository,
                                  ProductoService productoService) {
        this.facturaProveedorRepository = facturaProveedorRepository;
        this.facturaRepository = facturaRepository;
        this.detalleRepository = detalleRepository;
        this.stockRepository = stockRepository;
        this.proveedorRepository = proveedorRepository;
        this.productoRepository = productoRepository;
        this.formaDePagoRepository = formaDePagoRepository;
        this.productoService = productoService;
    }

    public void validarCompra(String proveedorId, List<String> productoIds, List<Integer> cantidades) {
        if (proveedorId == null || proveedorId.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar un proveedor para la orden de compra");
        }
        if (productoIds == null || productoIds.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos un producto para la orden de compra");
        }
        if (cantidades == null || cantidades.size() != productoIds.size()) {
            throw new IllegalArgumentException("La cantidad de ítems y productos no coincide");
        }
        for (Integer cant : cantidades) {
            if (cant == null || cant <= 0) {
                throw new IllegalArgumentException("La cantidad de cada producto pedido debe ser mayor a cero");
            }
        }
    }

    @Transactional
    public FacturaProveedor registrarCompraProveedor(String proveedorId,
                                                    Long numeroFacturaIngresado,
                                                    LocalDateTime fechaFactura,
                                                    String formaDePagoStr,
                                                    String estadoStr,
                                                    List<String> productoIds,
                                                    List<Integer> cantidades,
                                                    List<Double> costosUnitarios) {
        validarCompra(proveedorId, productoIds, cantidades);

        Proveedor proveedor = proveedorRepository.findActive(proveedorId.trim())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el proveedor activo con ID: " + proveedorId));

        // 1. Número correlativo o ingresado
        Long numeroFactura = numeroFacturaIngresado;
        if (numeroFactura == null || numeroFactura <= 0) {
            numeroFactura = facturaRepository.findTopByOrderByNumeroFacturaDesc()
                    .map(f -> f.getNumeroFactura() + 1)
                    .orElse(5001L);
        }

        // 2. Forma de pago
        TipoDePago tipoPago;
        try {
            tipoPago = (formaDePagoStr != null && !formaDePagoStr.trim().isEmpty())
                    ? TipoDePago.valueOf(formaDePagoStr.trim().toUpperCase())
                    : TipoDePago.TRANSFERENCIA;
        } catch (IllegalArgumentException e) {
            tipoPago = TipoDePago.TRANSFERENCIA;
        }

        final TipoDePago finalTipoPago = tipoPago;
        FormaDePago formaDePago = formaDePagoRepository.findByTipoPagoAndEliminadoFalse(finalTipoPago)
                .orElseGet(() -> formaDePagoRepository.save(
                        FormaDePago.builder()
                                .tipoPago(finalTipoPago)
                                .observacion("Registrado automáticamente en compra a proveedor")
                                .eliminado(false)
                                .build()
                ));

        // 3. Estado de la Factura
        EstadoFactura estado;
        try {
            estado = (estadoStr != null && !estadoStr.trim().isEmpty())
                    ? EstadoFactura.valueOf(estadoStr.trim().toUpperCase())
                    : EstadoFactura.PAGADA;
        } catch (IllegalArgumentException e) {
            estado = EstadoFactura.PAGADA;
        }

        // 4. Instanciar FacturaProveedor
        FacturaProveedor facturaProveedor = new FacturaProveedor();
        facturaProveedor.setProveedor(proveedor);
        facturaProveedor.setNumeroFactura(numeroFactura);
        facturaProveedor.setFechaFactura(fechaFactura != null ? fechaFactura : LocalDateTime.now());
        facturaProveedor.setEstado(estado);
        facturaProveedor.setFormaDePago(formaDePago);
        facturaProveedor.setTotalPagado(0.0);
        facturaProveedor.setEliminado(false);
        facturaProveedor.setDetalles(new HashSet<>());

        // 5. Procesar detalles de compra e incrementar stock
        double total = 0.0;
        List<Detalle> detallesList = new ArrayList<>();

        for (int i = 0; i < productoIds.size(); i++) {
            String prodId = productoIds.get(i);
            int cantidad = cantidades.get(i);
            double costoUnitario = (costosUnitarios != null && i < costosUnitarios.size() && costosUnitarios.get(i) != null)
                    ? costosUnitarios.get(i)
                    : 0.0;

            Producto producto = productoService.buscarPorId(prodId);
            if (costoUnitario <= 0.0) {
                try {
                    costoUnitario = productoService.obtenerPrecioActual(prodId);
                } catch (Exception ignored) {
                    costoUnitario = 0.0;
                }
            }

            double subtotal = Math.round(costoUnitario * cantidad * 100.0) / 100.0;
            total += subtotal;

            Detalle detalle = Detalle.builder()
                    .factura(facturaProveedor)
                    .producto(producto)
                    .cantidad(cantidad)
                    .subtotal(subtotal)
                    .eliminado(false)
                    .build();

            detallesList.add(detalle);
            facturaProveedor.getDetalles().add(detalle);

            // Incremento automático de stock
            producto.setStock(producto.getStock() + cantidad);
            productoRepository.save(producto);
        }

        facturaProveedor.setTotalPagado(Math.round(total * 100.0) / 100.0);
        FacturaProveedor guardada = facturaProveedorRepository.save(facturaProveedor);

        // Guardar detalles y registrar Stock trazable
        for (Detalle d : detallesList) {
            d.setFactura(guardada);
            Detalle detGuardado = detalleRepository.save(d);

            Stock stock = Stock.builder()
                    .cantidadActual(d.getCantidad())
                    .observacion("Ingreso por orden de compra a proveedor: " + proveedor.getRazonSocial() + " - Factura #" + guardada.getNumeroFactura())
                    .detalle(detGuardado)
                    .eliminado(false)
                    .build();
            stockRepository.save(stock);
        }

        return guardada;
    }

    @Transactional(readOnly = true)
    public List<FacturaProveedor> listarComprasProveedor() {
        return facturaProveedorRepository.findByEliminadoFalseOrderByFechaFacturaDesc();
    }

    @Transactional(readOnly = true)
    public FacturaProveedor buscarPorId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la orden de compra no puede ser nulo o vacío");
        }
        return facturaProveedorRepository.findActive(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la factura a proveedor con ID: " + id));
    }

    @Transactional
    public void eliminarCompraProveedor(String id) {
        FacturaProveedor factura = buscarPorId(id);
        factura.setEliminado(true);
        if (factura.getDetalles() != null) {
            for (Detalle d : factura.getDetalles()) {
                d.setEliminado(true);
            }
        }
        facturaProveedorRepository.save(factura);
    }
}
