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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
public class CompraProveedorService {

    private final FacturaProveedorRepository facturaProveedorRepository;
    private final FacturaRepository facturaRepository;
    private final DetalleRepository detalleRepository;
    private final StockRepository stockRepository;
    private final ProveedorRepository proveedorRepository;
    private final FormaDePagoRepository formaDePagoRepository;
    private final ProductoService productoService;
    private final StockService stockService;


    @Autowired
    public CompraProveedorService(FacturaProveedorRepository facturaProveedorRepository,
                                  FacturaRepository facturaRepository,
                                  DetalleRepository detalleRepository,
                                  StockRepository stockRepository,
                                  ProveedorRepository proveedorRepository,
                                  FormaDePagoRepository formaDePagoRepository,
                                  ProductoService productoService,
                                  StockService stockService) {
        this.facturaProveedorRepository = facturaProveedorRepository;
        this.facturaRepository = facturaRepository;
        this.detalleRepository = detalleRepository;
        this.stockRepository = stockRepository;
        this.proveedorRepository = proveedorRepository;
        this.formaDePagoRepository = formaDePagoRepository;
        this.productoService = productoService;
        this.stockService = stockService;
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

        Long numeroFactura = numeroFacturaIngresado;
        if (numeroFactura == null || numeroFactura <= 0) {
            numeroFactura = facturaRepository.findTopByOrderByNumeroFacturaDesc()
                    .map(f -> f.getNumeroFactura() + 1)
                    .orElse(5001L);
        }

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


        EstadoFactura estado;
        try {
            estado = (estadoStr != null && !estadoStr.trim().isEmpty())
                    ? EstadoFactura.valueOf(estadoStr.trim().toUpperCase())
                    : EstadoFactura.SIN_DEFINIR;
        } catch (IllegalArgumentException e) {
            estado = EstadoFactura.SIN_DEFINIR;
        }


        FacturaProveedor facturaProveedor = new FacturaProveedor();
        facturaProveedor.setProveedor(proveedor);
        facturaProveedor.setNumeroFactura(numeroFactura);
        facturaProveedor.setFechaFactura(fechaFactura != null ? fechaFactura : LocalDateTime.now());
        facturaProveedor.setEstado(estado);
        facturaProveedor.setFormaDePago(formaDePago);
        facturaProveedor.setTotalPagado(0.0);
        facturaProveedor.setEliminado(false);
        facturaProveedor.setDetalles(new HashSet<>());

        // procesar detalles de compra e incrementar stock solo si es entregada
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
                Double ultimoCosto = obtenerUltimoCostoUnitario(proveedorId, prodId);
                if (ultimoCosto != null && ultimoCosto > 0.0) {
                    costoUnitario = ultimoCosto;
                } else {
                    try {
                        costoUnitario = productoService.obtenerPrecioActual(prodId);
                    } catch (Exception ignored) {
                        costoUnitario = 0.0;
                    }
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

            // incremento de stock
            if (estado == EstadoFactura.ENTREGADA) {
                int stockActual = (stockService != null)
                        ? stockService.calcularStockActual(producto.getId())
                        : 0;
                int nuevoBalance = (stockService != null)
                        ? stockService.aumentarStock(stockActual, cantidad)
                        : stockActual + cantidad;

            }
        }

        facturaProveedor.setTotalPagado(Math.round(total * 100.0) / 100.0);
        FacturaProveedor guardada = facturaProveedorRepository.save(facturaProveedor);


        for (Detalle d : detallesList) {
            d.setFactura(guardada);
            Detalle detGuardado = detalleRepository.save(d);

            if (estado == EstadoFactura.ENTREGADA && d.getProducto() != null) {
                int stockActual = (stockService != null)
                        ? stockService.calcularStockActual(d.getProducto().getId())
                        : 0;
                int balance = (stockService != null)
                        ? stockService.aumentarStock(stockActual, d.getCantidad())
                        : stockActual + d.getCantidad();
                String obs = "Ingreso por orden de compra a proveedor: " + proveedor.getRazonSocial() + " - Factura #" + guardada.getNumeroFactura();
                if (stockService != null) {
                    stockService.crearStock(detGuardado, balance, obs);
                } else if (stockRepository != null) {
                    Optional<Stock> existente = (detGuardado.getId() != null)
                            ? stockRepository.findByDetalleId(detGuardado.getId())
                            : Optional.empty();
                    Stock stock = existente.orElseGet(() -> Stock.builder().detalle(detGuardado).build());
                    stock.setCantidadActual(balance);
                    stock.setObservacion(obs);
                    stock.setDetalle(detGuardado);
                    stock.setEliminado(false);
                    stockRepository.save(stock);
                }
            }
        }

        return guardada;
    }


    @Transactional
    public FacturaProveedor marcarComoEntregada(String facturaProveedorId) {
        FacturaProveedor factura = buscarPorId(facturaProveedorId);

        // alt [FacturaProveedor.getEstado() == EstadoFactura.ENTREGADA]
        if (factura.getEstado() == EstadoFactura.ENTREGADA) {
            throw new IllegalStateException("La Factura ya fue entregada previamente");
        }

        // Transacción: Actualización de existencias e inserción de registros Stock
        if (factura.getDetalles() != null) {
            for (Detalle detalle : factura.getDetalles()) {
                if (detalle != null && !detalle.isEliminado() && detalle.getProducto() != null) {
                    Producto producto = detalle.getProducto();
                    int stockActual = (stockService != null)
                            ? stockService.calcularStockActual(producto.getId())
                            : 0;

                    int nuevoBalance = (stockService != null)
                            ? stockService.aumentarStock(stockActual, detalle.getCantidad())
                            : stockActual + detalle.getCantidad();

                    if (stockService != null) {
                        stockService.crearStock(
                                detalle,
                                nuevoBalance,
                                "Ingreso por Entrega Proveedor - Factura N° " + factura.getNumeroFactura()
                        );
                    } else if (stockRepository != null) {
                        Optional<Stock> existente = (detalle.getId() != null)
                                ? stockRepository.findByDetalleId(detalle.getId())
                                : Optional.empty();
                        Stock stock = existente.orElseGet(() -> Stock.builder().detalle(detalle).build());
                        stock.setCantidadActual(nuevoBalance);
                        stock.setObservacion("Ingreso por Entrega Proveedor - Factura N° " + factura.getNumeroFactura());
                        stock.setDetalle(detalle);
                        stock.setEliminado(false);
                        stockRepository.save(stock);
                    }
                }
            }
        }

        factura.setEstado(EstadoFactura.ENTREGADA);
        return facturaProveedorRepository.save(factura);
    }

    @Transactional
    public FacturaProveedor marcarComoPagada(String facturaProveedorId) {
        return marcarComoEntregada(facturaProveedorId);
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

    ///btiene el costo unitario de compra más reciente para un producto y proveedor dados,
    @Transactional(readOnly = true)
    public Double obtenerUltimoCostoUnitario(String proveedorId, String productoId) {
        if (proveedorId == null || proveedorId.isBlank() || productoId == null || productoId.isBlank()) {
            return 0.0;
        }
        List<Detalle> detalles = detalleRepository.findDetallesByProveedorAndProductoOrderByFechaDesc(
                proveedorId.trim(), productoId.trim()
        );
        if (detalles != null && !detalles.isEmpty()) {
            Detalle masReciente = detalles.get(0);

            return calcularCostoUnitario(masReciente);
        }
        return 0.0;
    }

    ///calcula el costo o precio unitario a partir de un detalle
    public double calcularCostoUnitario(Detalle detalle) {
        if (detalle == null || detalle.getCantidad() <= 0) {
            return 0.0;
        }
        return Math.round((detalle.getSubtotal() / detalle.getCantidad()) * 100.0) / 100.0;
    }
}
