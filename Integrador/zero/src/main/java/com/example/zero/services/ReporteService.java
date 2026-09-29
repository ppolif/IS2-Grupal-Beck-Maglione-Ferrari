package com.example.zero.services;

import com.example.zero.dto.reporte.*;
import com.example.zero.entidades.compra.Detalle;
import com.example.zero.entidades.compra.Factura;
import com.example.zero.entidades.compraProveedor.Proveedor;
import com.example.zero.entidades.empresa.Contacto;
import com.example.zero.entidades.empresa.ContactoTelefonico;
import com.example.zero.entidades.empresa.Empresa;
import com.example.zero.entidades.producto.Producto;
import com.example.zero.repositories.DetalleRepository;
import com.example.zero.repositories.FacturaRepository;
import com.example.zero.services.producto.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * Servicio encargado de la generación y orquestación de datos para el módulo de Reportes.
 * Trabaja exclusivamente con DTOs como portadores de información.
 */
@Service
public class ReporteService {

    private final FacturaRepository facturaRepository;
    private final DetalleRepository detalleRepository;
    private final ProductoService productoService;
    private final StockService stockService;
    private final EmpresaService empresaService;

    @Autowired
    public ReporteService(FacturaRepository facturaRepository,
                          DetalleRepository detalleRepository,
                          ProductoService productoService,
                          StockService stockService,
                          @Autowired(required = false) EmpresaService empresaService) {
        this.facturaRepository = facturaRepository;
        this.detalleRepository = detalleRepository;
        this.productoService = productoService;
        this.stockService = stockService;
        this.empresaService = empresaService;
    }

    /**
     * Genera el Reporte de Ventas filtrando por rango de fechas.
     */
    @Transactional(readOnly = true)
    public ReporteVentasDTO generarReporteVentas(LocalDate fechaDesde, LocalDate fechaHasta) {
        LocalDate hoy = LocalDate.now();
        LocalDate desde = (fechaDesde != null) ? fechaDesde : hoy.withDayOfMonth(1);
        LocalDate hasta = (fechaHasta != null) ? fechaHasta : hoy;

        if (desde.isAfter(hasta)) {
            LocalDate temp = desde;
            desde = hasta;
            hasta = temp;
        }

        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.atTime(LocalTime.MAX);

        List<Factura> facturas = facturaRepository.findVentasEntreFechas(inicio, fin);
        List<ReporteVentaItemDTO> items = new ArrayList<>();

        double totalRecaudado = 0.0;
        int totalArticulosVendidos = 0;

        for (Factura f : facturas) {
            if (f == null || f.getDetalles() == null) continue;

            String clienteNombre = f.getCustomerName();
            String clienteId = f.getCustomerEmail();
            String formaPago = f.getPaymentMethod();
            String ordenNum = f.getOrderNumber();
            LocalDateTime fecha = f.getFechaFactura();

            for (Detalle d : f.getDetalles()) {
                if (d == null || d.isEliminado() || d.getProducto() == null) continue;

                Producto p = d.getProducto();
                String catNombre = d.getCategoryName();
                int cantidad = d.getCantidad();
                double unitPrice = d.getUnitPrice();
                double subtotal = d.getSubtotal();

                totalRecaudado += subtotal;
                totalArticulosVendidos += cantidad;

                items.add(ReporteVentaItemDTO.builder()
                        .facturaId(f.getId())
                        .numeroOrden(ordenNum)
                        .fechaCompra(fecha)
                        .productoCodigo(p.getCodigo())
                        .productoNombre(p.getNombre())
                        .categoriaNombre(catNombre)
                        .cantidad(cantidad)
                        .precioUnitario(unitPrice)
                        .subtotal(subtotal)
                        .formaPago(formaPago)
                        .clienteNombre(clienteNombre)
                        .clienteIdentificador(clienteId)
                        .build());
            }
        }

        int cantidadVentas = facturas.size();
        double ticketPromedio = cantidadVentas > 0
                ? Math.round((totalRecaudado / cantidadVentas) * 100.0) / 100.0
                : 0.0;

        totalRecaudado = Math.round(totalRecaudado * 100.0) / 100.0;

        return ReporteVentasDTO.builder()
                .fechaDesde(desde)
                .fechaHasta(hasta)
                .totalRecaudado(totalRecaudado)
                .cantidadVentas(cantidadVentas)
                .totalArticulosVendidos(totalArticulosVendidos)
                .ticketPromedio(ticketPromedio)
                .items(items)
                .build();
    }

    /**
     * Genera el Reporte de Productos con stock y cálculo del proveedor más conveniente para reposición.
     */
    @Transactional(readOnly = true)
    public ReporteProductosDTO generarReporteProductos(String sucursalId) {
        String sucursalNombre = "Sucursal Central";
        if (empresaService != null) {
            Empresa emp = empresaService.obtenerSucursalActiva();
            if (emp != null && emp.getRazonSocial() != null && !emp.getRazonSocial().isBlank()) {
                sucursalNombre = emp.getRazonSocial().trim();
            }
        }

        List<Producto> productos = productoService.listarActivos();
        List<ReporteProductoStockDTO> items = new ArrayList<>();

        int totalStockUnidades = 0;
        int stockBienCount = 0;
        int stockRegularCount = 0;
        int stockMaloCount = 0;

        for (Producto p : productos) {
            if (p == null) continue;

            int stock = (stockService != null) ? stockService.calcularStockActual(p.getId()) : 0;
            totalStockUnidades += stock;

            String estado;
            if (stock > 50) {
                estado = "BIEN";
                stockBienCount++;
            } else if (stock >= 20) {
                estado = "REGULAR";
                stockRegularCount++;
            } else {
                estado = "MALO";
                stockMaloCount++;
            }

            int faltante = Math.max(0, 50 - stock);

            // Buscar proveedor con menor costo registrado
            List<Detalle> detallesMenorCosto = detalleRepository.findDetallesConMenorCostoPorProducto(p.getId());
            String provId = null;
            String provNombre = "Sin historial de compra";
            String provTelefono = null;
            Double menorCosto = null;

            if (detallesMenorCosto != null && !detallesMenorCosto.isEmpty()) {
                Detalle mejorDetalle = detallesMenorCosto.get(0);
                menorCosto = mejorDetalle.getUnitPrice();
                Proveedor prov = mejorDetalle.getProveedor();
                if (prov == null && mejorDetalle.getFactura() != null) {
                    try {
                        Object unp = org.hibernate.Hibernate.unproxy(mejorDetalle.getFactura());
                        if (unp instanceof com.example.zero.entidades.compraProveedor.FacturaProveedor fp) {
                            prov = fp.getProveedor();
                        }
                    } catch (Exception ignored) {}
                }
                if (prov != null) {
                    provId = prov.getId();
                    provNombre = prov.getRazonSocial();
                    provTelefono = obtenerTelefonoProveedor(prov);
                }
            }

            // Construir enlace de WhatsApp al proveedor con menor costo si el stock es malo (< 20)
            String urlWhatsApp = "";
            if ("MALO".equals(estado)) {
                urlWhatsApp = armarUrlWhatsAppReposicion(p.getNombre(), p.getCodigo(), stock, faltante, provNombre, provTelefono);
            }

            String catNombre = (p.getSubCategoria() != null && p.getSubCategoria().getCategoria() != null)
                    ? p.getSubCategoria().getCategoria().getNombre()
                    : (p.getSubCategoria() != null ? p.getSubCategoria().getNombre() : "General");

            items.add(ReporteProductoStockDTO.builder()
                    .productoId(p.getId())
                    .codigo(p.getCodigo())
                    .nombre(p.getNombre())
                    .imagenUrl(p.getImagenUrl())
                    .talle(p.getTalle())
                    .categoria(catNombre)
                    .sucursal(sucursalNombre)
                    .stockActual(stock)
                    .estadoStock(estado)
                    .cantidadFaltante(faltante)
                    .proveedorId(provId)
                    .proveedorNombre(provNombre)
                    .proveedorTelefono(provTelefono)
                    .menorCosto(menorCosto)
                    .urlWhatsAppReposicion(urlWhatsApp)
                    .build());
        }

        return ReporteProductosDTO.builder()
                .sucursalNombre(sucursalNombre)
                .totalStockUnidades(totalStockUnidades)
                .stockBienCount(stockBienCount)
                .stockRegularCount(stockRegularCount)
                .stockMaloCount(stockMaloCount)
                .productos(items)
                .build();
    }

    /**
     * Genera el Reporte de Proveedores determinando el precio de costo más económico para cada producto.
     */
    @Transactional(readOnly = true)
    public ReporteProveedoresDTO generarReporteProveedores() {
        List<Producto> productos = productoService.listarActivos();
        List<ReporteProveedorPrecioDTO> items = new ArrayList<>();
        Map<String, Integer> contadorVictoriasProveedor = new HashMap<>();

        for (Producto p : productos) {
            if (p == null) continue;

            List<Detalle> detalles = detalleRepository.findDetallesConMenorCostoPorProducto(p.getId());

            String provId = null;
            String provRazon = "Sin compras previas";
            String provCuit = "N/A";
            String provTel = null;
            Double menorCosto = null;
            LocalDateTime fechaRef = null;
            Long numFacturaRef = null;
            String urlWs = "";

            if (detalles != null && !detalles.isEmpty()) {
                Detalle d = detalles.get(0);
                menorCosto = d.getUnitPrice();
                Proveedor prov = d.getProveedor();
                if (prov == null && d.getFactura() != null) {
                    try {
                        Object unp = org.hibernate.Hibernate.unproxy(d.getFactura());
                        if (unp instanceof com.example.zero.entidades.compraProveedor.FacturaProveedor fp) {
                            prov = fp.getProveedor();
                        }
                    } catch (Exception ignored) {}
                }
                if (prov != null) {
                    provId = prov.getId();
                    provRazon = prov.getRazonSocial();
                    provCuit = prov.getCuit();
                    provTel = obtenerTelefonoProveedor(prov);
                    contadorVictoriasProveedor.put(provRazon, contadorVictoriasProveedor.getOrDefault(provRazon, 0) + 1);
                }
                if (d.getFactura() != null) {
                    fechaRef = d.getFactura().getFechaFactura();
                    numFacturaRef = d.getFactura().getNumeroFactura();
                }

                urlWs = armarUrlWhatsAppConsulta(p.getNombre(), p.getCodigo(), provRazon, provTel);
            }

            items.add(ReporteProveedorPrecioDTO.builder()
                    .productoId(p.getId())
                    .productoCodigo(p.getCodigo())
                    .productoNombre(p.getNombre())
                    .productoDescripcion(p.getDescripcion())
                    .proveedorId(provId)
                    .proveedorRazonSocial(provRazon)
                    .proveedorCuit(provCuit)
                    .menorPrecioCosto(menorCosto)
                    .fechaFacturaReferencia(fechaRef)
                    .numeroFacturaReferencia(numFacturaRef)
                    .proveedorTelefono(provTel)
                    .urlWhatsAppContacto(urlWs)
                    .build());
        }

        // Proveedor más conveniente global
        String mejorProveedor = "Sin datos suficientes";
        int maxLiderados = 0;
        for (Map.Entry<String, Integer> entry : contadorVictoriasProveedor.entrySet()) {
            if (entry.getValue() > maxLiderados) {
                maxLiderados = entry.getValue();
                mejorProveedor = entry.getKey();
            }
        }

        return ReporteProveedoresDTO.builder()
                .totalProductos(productos.size())
                .proveedorMasConveniente(mejorProveedor)
                .productosConMejorPrecioProveedor(maxLiderados)
                .items(items)
                .build();
    }

    /**
     * Extrae el teléfono de contacto de un Proveedor.
     */
    public String obtenerTelefonoProveedor(Proveedor proveedor) {
        if (proveedor == null || proveedor.getContactos() == null) {
            return null;
        }
        for (Contacto c : proveedor.getContactos()) {
            if (c instanceof ContactoTelefonico ct && !ct.isEliminado() && ct.getTelefono() != null && !ct.getTelefono().isBlank()) {
                return ct.getTelefono().trim();
            }
        }
        return null;
    }

    /**
     * Construye la URL de WhatsApp para reposición de stock al proveedor más barato.
     */
    public String armarUrlWhatsAppReposicion(String prodNombre, String prodCodigo, int stockActual, int faltante, String provNombre, String telefono) {
        String numDestino = limpiarTelefono(telefono);
        if (numDestino.isEmpty()) {
            numDestino = "5492613072339"; // Número de contingencia predeterminado
        }

        String mensaje = String.format(
                "Hola %s! Desde ZERO Tienda Oficial deseamos hacer un pedido de reposición para el producto: %s (Código: %s). " +
                "Solicitamos %d unidades para alcanzar el 50%% de stock (Stock actual: %d unidades). Aguardamos cotización y confirmación de entrega.",
                (provNombre != null && !provNombre.isBlank() && !provNombre.startsWith("Sin")) ? provNombre : "Estimado Proveedor",
                prodNombre != null ? prodNombre : "Producto",
                prodCodigo != null ? prodCodigo : "",
                faltante,
                stockActual
        );

        try {
            return "https://wa.me/" + numDestino + "?text=" + URLEncoder.encode(mensaje, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            return "https://wa.me/" + numDestino;
        }
    }

    /**
     * Construye la URL de WhatsApp para consultar precios o solicitar compra a un proveedor.
     */
    public String armarUrlWhatsAppConsulta(String prodNombre, String prodCodigo, String provNombre, String telefono) {
        String numDestino = limpiarTelefono(telefono);
        if (numDestino.isEmpty()) {
            numDestino = "5492613072339";
        }

        String mensaje = String.format(
                "Hola %s! Nos comunicamos desde ZERO Tienda Oficial para consultar stock y disponibilidad de compra para el producto: %s (Código: %s).",
                (provNombre != null && !provNombre.isBlank() && !provNombre.startsWith("Sin")) ? provNombre : "Estimado Proveedor",
                prodNombre != null ? prodNombre : "Producto",
                prodCodigo != null ? prodCodigo : ""
        );

        try {
            return "https://wa.me/" + numDestino + "?text=" + URLEncoder.encode(mensaje, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            return "https://wa.me/" + numDestino;
        }
    }

    private String limpiarTelefono(String telefono) {
        if (telefono == null) return "";
        String limpio = telefono.replaceAll("[^0-9]", "");
        if (!limpio.isEmpty() && !limpio.startsWith("54") && limpio.length() >= 10) {
            limpio = "549" + limpio;
        }
        return limpio;
    }
}
