package com.DulceMayor.backend.services.reporte.exporter;

import com.DulceMayor.backend.dtos.reporte.ColumnaReporte;
import com.DulceMayor.backend.dtos.reporte.ReporteTabular;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/**
 * Producto concreto (ConcreteProduct del patrón Factory Method) encargado de exportar un
 * {@link ReporteTabular} a formato documento portátil PDF ({@code application/pdf}).
 *
 * <p><b>Patrón GoF aplicado: Factory Method (Creacional)</b></p>
 * <ul>
 *   <li><b>Participante GoF:</b> ConcreteProduct (Implementación concreta de {@link ReporteExporter}).</li>
 * </ul>
 */
@Component
public class PdfReporteExporter implements ReporteExporter {

    @Override
    public String getFormato() {
        return "pdf";
    }

    @Override
    public String getContentType() {
        return MediaType.APPLICATION_PDF_VALUE;
    }

    @Override
    public String getExtensionArchivo() {
        return "pdf";
    }

    @Override
    public byte[] exportar(ReporteTabular reporte) {
        StringBuilder texto = new StringBuilder();
        String titulo = limpiarTextoPdf(reporte.titulo() != null ? reporte.titulo() : "Reporte Dulce Mayor");
        texto.append(String.format(Locale.US, "BT /F1 14 Tf 40 800 Td (DULCE MAYOR - %s) Tj ET\n", titulo));

        String rango = String.format(Locale.US, "Generado: %s | Desde: %s | Hasta: %s",
                reporte.generadoEn() != null ? reporte.generadoEn().toLocalDate() : "-",
                reporte.desde() != null ? reporte.desde() : "-",
                reporte.hasta() != null ? reporte.hasta() : "-");
        texto.append(String.format(Locale.US, "BT /F1 9 Tf 40 780 Td (%s) Tj ET\n", limpiarTextoPdf(rango)));

        List<ColumnaReporte> columnas = reporte.columnas() != null ? reporte.columnas() : List.of();
        if (!columnas.isEmpty()) {
            String cabecera = columnas.stream()
                    .map(ColumnaReporte::titulo)
                    .collect(Collectors.joining(" | "));
            texto.append(String.format(Locale.US, "BT /F1 9 Tf 40 755 Td (%s) Tj ET\n", limpiarTextoPdf(cabecera)));
        }

        int y = 735;
        List<Map<String, Object>> filas = reporte.filas() != null ? reporte.filas() : List.of();
        for (Map<String, Object> fila : filas) {
            String linea = columnas.stream()
                    .map(col -> {
                        Object val = fila.get(col.clave());
                        return val != null ? String.valueOf(val) : "";
                    })
                    .collect(Collectors.joining(" | "));
            texto.append(String.format(Locale.US, "BT /F1 8 Tf 40 %d Td (%s) Tj ET\n", y, limpiarTextoPdf(linea)));
            y -= 15;
            if (y < 60) {
                break;
            }
        }

        String streamContent = texto.toString();
        String pdf = "%PDF-1.4\n"
                + "1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj\n"
                + "2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj\n"
                + "3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >> endobj\n"
                + "4 0 obj << /Length " + streamContent.length() + " >> stream\n"
                + streamContent
                + "endstream endobj\n"
                + "5 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> endobj\n"
                + "xref\n0 6\n0000000000 65535 f \n"
                + "trailer << /Size 6 /Root 1 0 R >>\n"
                + "startxref\n0\n%%EOF";
        return pdf.getBytes(StandardCharsets.ISO_8859_1);
    }

    private String limpiarTextoPdf(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace("(", "").replace(")", "").replace("\\", "");
    }
}

