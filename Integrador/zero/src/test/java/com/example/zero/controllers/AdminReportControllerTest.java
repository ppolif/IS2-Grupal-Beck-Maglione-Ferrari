package com.example.zero.controllers;

import com.example.zero.dto.reporte.*;
import com.example.zero.services.ReporteExportService;
import com.example.zero.services.ReporteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminReportControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReporteService reporteService;

    @Mock
    private ReporteExportService exportService;

    @InjectMocks
    private AdminReportController adminReportController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminReportController).build();
    }

    @Test
    void testVerReportesRetornaVistaYModelos() throws Exception {
        ReporteVentasDTO ventasDTO = ReporteVentasDTO.builder()
                .items(Collections.emptyList())
                .totalRecaudado(0.0)
                .build();

        ReporteProductosDTO productosDTO = ReporteProductosDTO.builder()
                .productos(Collections.emptyList())
                .build();

        ReporteProveedoresDTO proveedoresDTO = ReporteProveedoresDTO.builder()
                .items(Collections.emptyList())
                .build();

        when(reporteService.generarReporteVentas(any(LocalDate.class), any(LocalDate.class))).thenReturn(ventasDTO);
        when(reporteService.generarReporteProductos(any())).thenReturn(productosDTO);
        when(reporteService.generarReporteProveedores()).thenReturn(proveedoresDTO);

        mockMvc.perform(get("/admin/reportes").param("tab", "ventas"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/reports"))
                .andExpect(model().attributeExists("reporteVentas"))
                .andExpect(model().attributeExists("reporteProductos"))
                .andExpect(model().attributeExists("reporteProveedores"))
                .andExpect(model().attribute("activeTab", "ventas"));
    }

    @Test
    void testExportarVentasCsv() throws Exception {
        ReporteVentasDTO ventasDTO = ReporteVentasDTO.builder().build();
        when(reporteService.generarReporteVentas(any(LocalDate.class), any(LocalDate.class))).thenReturn(ventasDTO);
        when(exportService.exportarVentasCsv(any(ReporteVentasDTO.class))).thenReturn("csv-data".getBytes());

        mockMvc.perform(get("/admin/reportes/exportar")
                        .param("tipo", "ventas")
                        .param("formato", "csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("reporte_ventas")))
                .andExpect(content().contentType(MediaType.parseMediaType("text/csv; charset=UTF-8")));
    }

    @Test
    void testExportarProductosXlsx() throws Exception {
        ReporteProductosDTO productosDTO = ReporteProductosDTO.builder().build();
        when(reporteService.generarReporteProductos(any())).thenReturn(productosDTO);
        when(exportService.exportarProductosXlsx(any(ReporteProductosDTO.class))).thenReturn("xlsx-data".getBytes());

        mockMvc.perform(get("/admin/reportes/exportar")
                        .param("tipo", "productos")
                        .param("formato", "xlsx"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("reporte_productos")))
                .andExpect(content().contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")));
    }
}
