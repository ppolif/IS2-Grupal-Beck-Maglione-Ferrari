package com.DulceMayor.backend.services.reporte.exporter;

import com.DulceMayor.backend.dtos.reporte.ColumnaReporte;
import com.DulceMayor.backend.dtos.reporte.ReporteTabular;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.stereotype.Component;

/**
 * Producto concreto (ConcreteProduct del patrón Factory Method) encargado de exportar un
 * {@link ReporteTabular} a una planilla de cálculo OpenXML Excel ({@code .xlsx}).
 *
 * <p><b>Patrón GoF aplicado: Factory Method (Creacional)</b></p>
 * <ul>
 *   <li><b>Participante GoF:</b> ConcreteProduct (Implementación concreta de {@link ReporteExporter}).</li>
 * </ul>
 */
@Component
public class ExcelReporteExporter implements ReporteExporter {

    public static final String CONTENT_TYPE_XLSX =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    @Override
    public String getFormato() {
        return "xlsx";
    }

    @Override
    public String getContentType() {
        return CONTENT_TYPE_XLSX;
    }

    @Override
    public String getExtensionArchivo() {
        return "xlsx";
    }

    @Override
    public byte[] exportar(ReporteTabular reporte) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos, StandardCharsets.UTF_8)) {

            escribirEntradaZip(zos, "[Content_Types].xml", construirContentTypesXml());
            escribirEntradaZip(zos, "_rels/.rels", construirRootRelsXml());
            escribirEntradaZip(zos, "xl/workbook.xml", construirWorkbookXml());
            escribirEntradaZip(zos, "xl/_rels/workbook.xml.rels", construirWorkbookRelsXml());
            escribirEntradaZip(zos, "xl/worksheets/sheet1.xml", construirSheetXml(reporte));

            zos.finish();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Error al generar el archivo Excel (.xlsx) del reporte", e);
        }
    }

    private void escribirEntradaZip(ZipOutputStream zos, String nombre, String contenidoXml) throws IOException {
        ZipEntry entry = new ZipEntry(nombre);
        zos.putNextEntry(entry);
        zos.write(contenidoXml.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private String construirContentTypesXml() {
        return """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
                  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
                  <Default Extension="xml" ContentType="application/xml"/>
                  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
                  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
                </Types>
                """;
    }

    private String construirRootRelsXml() {
        return """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
                </Relationships>
                """;
    }

    private String construirWorkbookXml() {
        return """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
                          xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
                  <sheets>
                    <sheet name="Reporte" sheetId="1" r:id="rId1"/>
                  </sheets>
                </workbook>
                """;
    }

    private String construirWorkbookRelsXml() {
        return """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
                </Relationships>
                """;
    }

    private String construirSheetXml(ReporteTabular reporte) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
        sb.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">\n");
        sb.append("  <sheetData>\n");

        List<ColumnaReporte> columnas = reporte.columnas() != null ? reporte.columnas() : List.of();
        int rowNum = 1;

        // Fila 1: Título del reporte
        sb.append("    <row r=\"").append(rowNum).append("\">");
        sb.append(construirCeldaTexto("A" + rowNum, reporte.titulo() != null ? reporte.titulo() : "Reporte"));
        sb.append("</row>\n");
        rowNum++;

        // Fila 2: Encabezados de columnas
        if (!columnas.isEmpty()) {
            sb.append("    <row r=\"").append(rowNum).append("\">");
            for (int c = 0; c < columnas.size(); c++) {
                String ref = nombreColumnaExcel(c) + rowNum;
                sb.append(construirCeldaTexto(ref, columnas.get(c).titulo()));
            }
            sb.append("</row>\n");
            rowNum++;
        }

        // Filas de datos
        List<Map<String, Object>> filas = reporte.filas() != null ? reporte.filas() : List.of();
        for (Map<String, Object> fila : filas) {
            sb.append("    <row r=\"").append(rowNum).append("\">");
            for (int c = 0; c < columnas.size(); c++) {
                String ref = nombreColumnaExcel(c) + rowNum;
                Object valor = fila.get(columnas.get(c).clave());
                if (valor instanceof Number numero) {
                    sb.append(String.format(Locale.US, "<c r=\"%s\" t=\"n\"><v>%s</v></c>", ref, numero));
                } else {
                    sb.append(construirCeldaTexto(ref, valor != null ? String.valueOf(valor) : ""));
                }
            }
            sb.append("</row>\n");
            rowNum++;
        }

        sb.append("  </sheetData>\n");
        sb.append("</worksheet>");
        return sb.toString();
    }

    private String construirCeldaTexto(String ref, String texto) {
        return "<c r=\"" + ref + "\" t=\"inlineStr\"><is><t>" + escaparXml(texto) + "</t></is></c>";
    }

    private String nombreColumnaExcel(int indexZeroBased) {
        StringBuilder col = new StringBuilder();
        int n = indexZeroBased;
        while (n >= 0) {
            col.insert(0, (char) ('A' + (n % 26)));
            n = (n / 26) - 1;
        }
        return col.toString();
    }

    private String escaparXml(String input) {
        if (input == null) {
            return "";
        }
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}

