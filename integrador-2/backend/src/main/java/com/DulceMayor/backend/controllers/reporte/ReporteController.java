package com.DulceMayor.backend.controllers.reporte;

import com.DulceMayor.backend.dtos.reporte.ReporteTabular;
import com.DulceMayor.backend.services.reporte.ReporteLecturaService;
import com.DulceMayor.backend.services.reporte.exporter.ReporteExporter;
import java.time.LocalDate;
import java.util.Set;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para la generación y descarga de reportes operativos y gerenciales
 * ({@code /reportes/**}) en formato JSON, PDF o Excel ({@code .xlsx}) según {@code 04-openapi.yaml}.
 */
@RestController
@RequestMapping({"/reportes", "/api/v1/reportes"})
public class ReporteController {

    private static final Set<String> FORMATOS_JSON_PDF = Set.of("json", "pdf");
    private static final Set<String> FORMATOS_JSON_PDF_XLSX = Set.of("json", "pdf", "xlsx");

    private final ReporteLecturaService reporteLecturaService;

    public ReporteController(ReporteLecturaService reporteLecturaService) {
        this.reporteLecturaService = reporteLecturaService;
    }

    @GetMapping("/facturacion")
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<?> reporteFacturacion(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "json") String formato) {
        ReporteTabular reporte = reporteLecturaService.generarReporteFacturacion(desde, hasta);
        return responderSegunFormato(reporte, formato, FORMATOS_JSON_PDF, "reporte-facturacion");
    }

    @GetMapping("/minoristas")
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<?> reporteMinoristas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "json") String formato) {
        ReporteTabular reporte = reporteLecturaService.generarReporteMinoristas(desde, hasta);
        return responderSegunFormato(reporte, formato, FORMATOS_JSON_PDF_XLSX, "reporte-minoristas");
    }

    @GetMapping("/pedidos")
    @PreAuthorize("hasAnyRole('JEFE', 'ADMINISTRATIVO')")
    public ResponseEntity<?> reportePedidos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "json") String formato) {
        ReporteTabular reporte = reporteLecturaService.generarReportePedidos(desde, hasta, estado);
        return responderSegunFormato(reporte, formato, FORMATOS_JSON_PDF, "reporte-pedidos");
    }

    @GetMapping("/productos-mas-vendidos")
    @PreAuthorize("hasRole('JEFE')")
    public ResponseEntity<?> reporteProductosMasVendidos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "10") Integer limite,
            @RequestParam(defaultValue = "json") String formato) {
        ReporteTabular reporte = reporteLecturaService.generarReporteProductosMasVendidos(desde, hasta, limite);
        return responderSegunFormato(reporte, formato, FORMATOS_JSON_PDF_XLSX, "reporte-productos-mas-vendidos");
    }

    @GetMapping("/rendimiento-preventores")
    @PreAuthorize("hasRole('JEFE')")
    public ResponseEntity<?> reporteRendimientoPreventores(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "json") String formato) {
        ReporteTabular reporte = reporteLecturaService.generarReporteRendimientoPreventores(desde, hasta);
        return responderSegunFormato(reporte, formato, FORMATOS_JSON_PDF_XLSX, "reporte-rendimiento-preventores");
    }

    @GetMapping("/ventas-historico")
    @PreAuthorize("hasRole('JEFE')")
    public ResponseEntity<?> reporteVentasHistorico(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String departamentoId,
            @RequestParam(required = false) String preventorId,
            @RequestParam(defaultValue = "json") String formato) {
        ReporteTabular reporte = reporteLecturaService.generarReporteVentasHistorico(
                desde, hasta, departamentoId, preventorId
        );
        return responderSegunFormato(reporte, formato, FORMATOS_JSON_PDF_XLSX, "reporte-ventas-historico");
    }

    private ResponseEntity<?> responderSegunFormato(
            ReporteTabular reporte,
            String formato,
            Set<String> formatosPermitidos,
            String nombreBaseArchivo) {
        if (formato == null || formato.isBlank() || "json".equalsIgnoreCase(formato.trim())) {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(reporte);
        }

        ReporteExporter exporter = reporteLecturaService.obtenerExporter(formato, formatosPermitidos);
        byte[] contenidoBinario = exporter.exportar(reporte);
        String filename = nombreBaseArchivo + "." + exporter.getExtensionArchivo();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(exporter.getContentType()))
                .body(contenidoBinario);
    }
}

