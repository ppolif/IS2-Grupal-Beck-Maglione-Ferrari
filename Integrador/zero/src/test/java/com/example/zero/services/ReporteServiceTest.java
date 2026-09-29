package com.example.zero.services;

import com.example.zero.dto.reporte.*;
import com.example.zero.entidades.compra.Detalle;
import com.example.zero.entidades.compra.Factura;
import com.example.zero.entidades.compraProveedor.FacturaProveedor;
import com.example.zero.entidades.compraProveedor.Proveedor;
import com.example.zero.entidades.producto.Categoria;
import com.example.zero.entidades.producto.Producto;
import com.example.zero.entidades.producto.SubCategoria;
import com.example.zero.repositories.DetalleRepository;
import com.example.zero.repositories.FacturaRepository;
import com.example.zero.services.producto.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock
    private FacturaRepository facturaRepository;
    @Mock
    private DetalleRepository detalleRepository;
    @Mock
    private ProductoService productoService;
    @Mock
    private StockService stockService;
    @Mock
    private EmpresaService empresaService;

    @InjectMocks
    private ReporteService reporteService;

    private Producto producto;

    @BeforeEach
    void setUp() {
        Categoria cat = new Categoria();
        cat.setNombre("Bebidas");

        SubCategoria subcat = new SubCategoria();
        subcat.setNombre("Gaseosas");
        subcat.setCategoria(cat);

        producto = new Producto();
        producto.setId("prod-1");
        producto.setNombre("Coca Cola 1.5L");
        producto.setCodigo("CC-15");
        producto.setSubCategoria(subcat);
    }

    @Test
    void testGenerarReporteVentas() {
        LocalDate desde = LocalDate.of(2026, 3, 1);
        LocalDate hasta = LocalDate.of(2026, 3, 31);

        Factura factura = new Factura() {};
        factura.setId("fac-1");
        factura.setNumeroFactura(101L);
        factura.setFechaFactura(LocalDateTime.of(2026, 3, 10, 14, 30));

        Detalle detalle = new Detalle();
        detalle.setId("det-1");
        detalle.setFactura(factura);
        detalle.setProducto(producto);
        detalle.setCantidad(2);
        detalle.setSubtotal(1000.0);
        detalle.setEliminado(false);

        factura.setDetalles(Set.of(detalle));

        when(facturaRepository.findVentasEntreFechas(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(factura));

        ReporteVentasDTO dto = reporteService.generarReporteVentas(desde, hasta);

        assertNotNull(dto);
        assertEquals(1, dto.getItems().size());
        assertEquals(1, dto.getCantidadVentas());
        assertEquals(2, dto.getTotalArticulosVendidos());
        assertEquals(1000.0, dto.getTotalRecaudado());
        assertEquals("Coca Cola 1.5L", dto.getItems().get(0).getProductoNombre());
    }

    @Test
    void testGenerarReporteProductosStockMaloYWhatsApp() {
        Proveedor prov = new Proveedor();
        prov.setId("prov-1");
        prov.setRazonSocial("Distribuidora Bebidas SA");
        prov.setCuit("30-12345678-9");

        FacturaProveedor facProv = new FacturaProveedor();
        facProv.setProveedor(prov);

        Detalle detalleCompra = new Detalle();
        detalleCompra.setProducto(producto);
        detalleCompra.setFactura(facProv);
        detalleCompra.setCantidad(10);
        detalleCompra.setSubtotal(2000.0); // 200.0 c/u

        when(productoService.listarActivos()).thenReturn(List.of(producto));
        when(stockService.calcularStockActual("prod-1")).thenReturn(10);
        when(detalleRepository.findDetallesConMenorCostoPorProducto("prod-1")).thenReturn(List.of(detalleCompra));

        ReporteProductosDTO dto = reporteService.generarReporteProductos(null);

        assertNotNull(dto);
        assertEquals(1, dto.getProductos().size());
        ReporteProductoStockDTO item = dto.getProductos().get(0);
        assertEquals(10, item.getStockActual());
        assertEquals("MALO", item.getEstadoStock());
        assertEquals(40, item.getCantidadFaltante());
        assertNotNull(item.getUrlWhatsAppReposicion());
        String decodedUrl = URLDecoder.decode(item.getUrlWhatsAppReposicion(), StandardCharsets.UTF_8);
        assertTrue(decodedUrl.contains("Distribuidora Bebidas SA"));
        assertTrue(decodedUrl.contains("40"));
    }

    @Test
    void testGenerarReporteProveedoresMenorCosto() {
        Proveedor prov1 = new Proveedor();
        prov1.setId("prov-1");
        prov1.setRazonSocial("Proveedor Barato");
        prov1.setCuit("20-22222222-2");

        FacturaProveedor facProv = new FacturaProveedor();
        facProv.setProveedor(prov1);

        Detalle d1 = new Detalle();
        d1.setProducto(producto);
        d1.setFactura(facProv);
        d1.setCantidad(10);
        d1.setSubtotal(2500.0); // 250.0 c/u

        when(productoService.listarActivos()).thenReturn(List.of(producto));
        when(detalleRepository.findDetallesConMenorCostoPorProducto("prod-1")).thenReturn(List.of(d1));

        ReporteProveedoresDTO dto = reporteService.generarReporteProveedores();

        assertNotNull(dto);
        assertEquals(1, dto.getItems().size());
        ReporteProveedorPrecioDTO item = dto.getItems().get(0);
        assertEquals("Coca Cola 1.5L", item.getProductoNombre());
        assertEquals("Proveedor Barato", item.getProveedorRazonSocial());
        assertEquals(250.0, item.getMenorPrecioCosto());
    }
}
