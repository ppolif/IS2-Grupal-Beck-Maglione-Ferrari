package com.example.zero.controllers;

import com.example.zero.dto.reporte.ReporteProductosDTO;
import com.example.zero.dto.reporte.ReporteProveedoresDTO;
import com.example.zero.dto.reporte.ReporteVentasDTO;
import com.example.zero.services.ReporteExportService;
import com.example.zero.services.ReporteService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

//controlador de reportes
@Controller
public class AdminReportController {

    private final ReporteService reporteService;
    private final ReporteExportService reporteExportService;

    public AdminReportController(ReporteService reporteService, ReporteExportService reporteExportService) {
        this.reporteService = reporteService;
        this.reporteExportService = reporteExportService;
    }


    @GetMapping("/admin/reportes")
    public String showReports(Model model,
                              @RequestParam(name = "tab", defaultValue = "ventas") String tab,
                              @RequestParam(name = "desde", required = false)
                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                              @RequestParam(name = "hasta", required = false)
                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                              @RequestParam(name = "sucursalId", required = false) String sucursalId) {

        LocalDate hoy = LocalDate.now();
        LocalDate fechaInicio = (desde != null) ? desde : hoy.withDayOfMonth(1);
        LocalDate fechaFin = (hasta != null) ? hasta : hoy;

        ReporteVentasDTO reporteVentas = reporteService.generarReporteVentas(fechaInicio, fechaFin);
        ReporteProductosDTO reporteProductos = reporteService.generarReporteProductos(sucursalId);
        ReporteProveedoresDTO reporteProveedores = reporteService.generarReporteProveedores();

        model.addAttribute("activeTab", tab != null ? tab.toLowerCase() : "ventas");
        model.addAttribute("fechaDesde", fechaInicio);
        model.addAttribute("fechaHasta", fechaFin);
        model.addAttribute("reporteVentas", reporteVentas);
        model.addAttribute("reporteProductos", reporteProductos);
        model.addAttribute("reporteProveedores", reporteProveedores);

        return "admin/reports";
    }

    ///descarga de informe en formato .xlsx o .csv
    @GetMapping("/admin/reportes/exportar")
    public ResponseEntity<byte[]> exportarInforme(
            @RequestParam(name = "tipo", defaultValue = "ventas") String tipo,
            @RequestParam(name = "formato", defaultValue = "xlsx") String formato,
            @RequestParam(name = "desde", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(name = "hasta", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(name = "sucursalId", required = false) String sucursalId) {

        String tipoNorm = tipo != null ? tipo.toLowerCase().trim() : "ventas";
        String formatoNorm = formato != null ? formato.toLowerCase().trim() : "xlsx";
        boolean isCsv = "csv".equals(formatoNorm);

        byte[] contenido;
        String filename;
        String timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        switch (tipoNorm) {
            case "productos":
                ReporteProductosDTO prodDTO = reporteService.generarReporteProductos(sucursalId);
                contenido = isCsv ? reporteExportService.exportarProductosCsv(prodDTO) : reporteExportService.exportarProductosXlsx(prodDTO);
                filename = "reporte_productos_" + timestamp + (isCsv ? ".csv" : ".xlsx");
                break;

            case "proveedores":
                ReporteProveedoresDTO provDTO = reporteService.generarReporteProveedores();
                contenido = isCsv ? reporteExportService.exportarProveedoresCsv(provDTO) : reporteExportService.exportarProveedoresXlsx(provDTO);
                filename = "reporte_proveedores_" + timestamp + (isCsv ? ".csv" : ".xlsx");
                break;

            case "ventas":
            default:
                LocalDate hoy = LocalDate.now();
                LocalDate fDesde = (desde != null) ? desde : hoy.withDayOfMonth(1);
                LocalDate fHasta = (hasta != null) ? hasta : hoy;
                ReporteVentasDTO ventasDTO = reporteService.generarReporteVentas(fDesde, fHasta);
                contenido = isCsv ? reporteExportService.exportarVentasCsv(ventasDTO) : reporteExportService.exportarVentasXlsx(ventasDTO);
                filename = "reporte_ventas_" + timestamp + (isCsv ? ".csv" : ".xlsx");
                break;
        }

        MediaType mediaType = isCsv
                ? new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8)
                : MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .contentType(mediaType)
                .body(contenido);
    }
}
