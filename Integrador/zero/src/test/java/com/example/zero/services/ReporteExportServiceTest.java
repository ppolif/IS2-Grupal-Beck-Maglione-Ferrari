package com.example.zero.services;

import com.example.zero.dto.reporte.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReporteExportServiceTest {

    private ReporteExportService exportService;

    @BeforeEach
    void setUp() {
        exportService = new ReporteExportService();
    }

    @Test
    void testExportarVentasCsvYXlsx() {
        ReporteVentasDTO dto = ReporteVentasDTO.builder()
                .fechaDesde(LocalDate.of(2026, 3, 1))
                .fechaHasta(LocalDate.of(2026, 3, 31))
                .totalRecaudado(1500.0)
                .cantidadVentas(1)
                .totalArticulosVendidos(3)
                .ticketPromedio(1500.0)
                .build();

        ReporteVentaItemDTO item = ReporteVentaItemDTO.builder()
                .fechaCompra(LocalDateTime.of(2026, 3, 15, 10, 0))
                .numeroOrden("FAC-100")
                .productoCodigo("ART-1")
                .productoNombre("Remera")
                .categoriaNombre("Indumentaria")
                .cantidad(3)
                .precioUnitario(500.0)
                .formaPago("EFECTIVO")
                .subtotal(1500.0)
                .clienteNombre("Juan Perez")
                .clienteIdentificador("juan@test.com")
                .build();
        dto.setItems(List.of(item));

        byte[] csv = exportService.exportarVentasCsv(dto);
        assertNotNull(csv);
        String csvText = new String(csv);
        assertTrue(csvText.contains("FAC-100"));
        assertTrue(csvText.contains("Remera"));
        assertTrue(csvText.contains("Indumentaria"));

        byte[] xlsx = exportService.exportarVentasXlsx(dto);
        assertNotNull(xlsx);
        assertTrue(xlsx.length > 100);
    }

    @Test
    void testExportarProductosCsvYXlsx() {
        ReporteProductosDTO dto = ReporteProductosDTO.builder()
                .sucursalNombre("Sucursal Central")
                .totalStockUnidades(10)
                .stockBienCount(0)
                .stockRegularCount(0)
                .stockMaloCount(1)
                .build();

        ReporteProductoStockDTO prod = ReporteProductoStockDTO.builder()
                .productoId("p1")
                .codigo("COD-1")
                .nombre("Pantalón")
                .categoria("Indumentaria")
                .sucursal("Sucursal Central")
                .stockActual(10)
                .estadoStock("MALO")
                .cantidadFaltante(40)
                .proveedorNombre("Proveedor Telas")
                .menorCosto(2000.0)
                .proveedorTelefono("123456789")
                .urlWhatsAppReposicion("https://wa.me/123456789")
                .build();
        dto.setProductos(List.of(prod));

        byte[] csv = exportService.exportarProductosCsv(dto);
        assertNotNull(csv);
        String csvText = new String(csv);
        assertTrue(csvText.contains("Pantalón"));
        assertTrue(csvText.contains("MALO"));

        byte[] xlsx = exportService.exportarProductosXlsx(dto);
        assertNotNull(xlsx);
        assertTrue(xlsx.length > 100);
    }

    @Test
    void testExportarProveedoresCsvYXlsx() {
        ReporteProveedoresDTO dto = ReporteProveedoresDTO.builder()
                .totalProductos(1)
                .proveedorMasConveniente("Distribuidora Calzados")
                .productosConMejorPrecioProveedor(1)
                .build();

        ReporteProveedorPrecioDTO item = ReporteProveedorPrecioDTO.builder()
                .productoCodigo("ZAP-1")
                .productoNombre("Zapatillas")
                .proveedorRazonSocial("Distribuidora Calzados")
                .proveedorCuit("30-99999999-9")
                .menorPrecioCosto(12000.0)
                .fechaFacturaReferencia(LocalDateTime.of(2026, 2, 20, 11, 0))
                .numeroFacturaReferencia(555L)
                .proveedorTelefono("1144556677")
                .build();
        dto.setItems(List.of(item));

        byte[] csv = exportService.exportarProveedoresCsv(dto);
        assertNotNull(csv);
        String csvText = new String(csv);
        assertTrue(csvText.contains("Zapatillas"));
        assertTrue(csvText.contains("Distribuidora Calzados"));

        byte[] xlsx = exportService.exportarProveedoresXlsx(dto);
        assertNotNull(xlsx);
        assertTrue(xlsx.length > 100);
    }
}
