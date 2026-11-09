package com.DulceMayor.backend.services.reporte.exporter;

import com.DulceMayor.backend.config.ValidacionException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Fábrica creacional (Creator / Factory Method) que resuelve e instancia el {@link ReporteExporter}
 * adecuado según el parámetro {@code formato} solicitado en la petición HTTP
 * (sección 4.4 de {@code 03-arquitectura.md} y sección 8 de {@code GENERAL.md}).
 *
 * <p><b>Patrón GoF aplicado: Factory Method (Creacional)</b></p>
 * <ul>
 *   <li><b>Participante GoF:</b> Creator / Factory (Clase que provee el método de fábrica {@link #obtenerExporter}).</li>
 *   <li><b>Productos (Product / ConcreteProduct):</b> {@link ReporteExporter}, {@link PdfReporteExporter}
 *       y {@link ExcelReporteExporter}.</li>
 *   <li><b>Justificación:</b> El sistema necesita exportar el mismo {@code ReporteTabular} a PDF o Excel
 *       según el parámetro de la petición. La fábrica centraliza la selección del exportador sin acoplar
 *       los controladores ni el servicio de lectura a las clases concretas de generación documental.</li>
 * </ul>
 */
@Component
public class ReporteExporterFactory {

    private final Map<String, ReporteExporter> exportersPorFormato;

    public ReporteExporterFactory(List<ReporteExporter> exporters) {
        this.exportersPorFormato = new HashMap<>();
        if (exporters != null) {
            for (ReporteExporter exporter : exporters) {
                this.exportersPorFormato.put(
                        exporter.getFormato().toLowerCase(Locale.ROOT),
                        exporter
                );
            }
        }
    }

    /**
     * Método de fábrica (Factory Method) que devuelve el {@link ReporteExporter} correspondiente
     * al formato indicado, validando previamente que esté habilitado para el reporte consultado.
     */
    public ReporteExporter obtenerExporter(String formato, Set<String> formatosPermitidos) {
        if (formato == null || formato.isBlank()) {
            throw new ValidacionException("El formato de exportación es obligatorio");
        }
        String formatoNorm = formato.trim().toLowerCase(Locale.ROOT);
        if (formatosPermitidos != null && !formatosPermitidos.contains(formatoNorm)) {
            throw new ValidacionException(
                    "El formato '" + formato + "' no está permitido para este reporte. Formatos válidos: "
                            + formatosPermitidos
            );
        }
        ReporteExporter exporter = exportersPorFormato.get(formatoNorm);
        if (exporter == null) {
            throw new ValidacionException("No existe exportador configurado para el formato: " + formato);
        }
        return exporter;
    }
}

