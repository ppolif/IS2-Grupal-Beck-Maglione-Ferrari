package com.example.persona.services;

import com.example.persona.dtos.LibroDto;
import com.example.persona.dtos.PersonaDto;
import com.example.persona.dtos.PrestamoDto;
import com.example.persona.entities.Persona;
import com.example.persona.entities.Prestamo;
import com.example.persona.repositories.PrestamoRepository;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReporteServiceImpl implements ReporteService {

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private LibroService libroService;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public byte[] generarPdfPersonasConPrestamos() throws Exception {
        List<Persona> personas = prestamoRepository.findPersonasConHistorialPrestamos();

        Document document = new Document(PageSize.A4, 36, 36, 40, 40);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);

        document.open();

        // Fuentes iText específicas
        com.itextpdf.text.Font fontTituloPrincipal = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new BaseColor(33, 37, 41));
        com.itextpdf.text.Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA, 10, new BaseColor(108, 117, 125));
        com.itextpdf.text.Font fontPersonaHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new BaseColor(13, 110, 253));
        com.itextpdf.text.Font fontPersonaInfo = FontFactory.getFont(FontFactory.HELVETICA, 9, new BaseColor(50, 50, 50));
        com.itextpdf.text.Font fontTableHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.WHITE);
        com.itextpdf.text.Font fontTableBody = FontFactory.getFont(FontFactory.HELVETICA, 8, new BaseColor(30, 30, 30));
        com.itextpdf.text.Font fontBadgeActivo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new BaseColor(25, 135, 84));
        com.itextpdf.text.Font fontBadgeDevuelto = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new BaseColor(108, 117, 125));

        // Título del Documento
        Paragraph titulo = new Paragraph("REPORTE DE PERSONAS CON PRÉSTAMOS SOLICITADOS", fontTituloPrincipal);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String fechaEmision = LocalDateTime.now().format(dtf);
        Paragraph subtitulo = new Paragraph("Emitido el: " + fechaEmision + " | Total de personas con préstamos registrados: " + personas.size(), fontSubtitulo);
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        subtitulo.setSpacingAfter(20);
        document.add(subtitulo);

        if (personas.isEmpty()) {
            Paragraph vacio = new Paragraph("No se registran personas con historial de préstamos solicitados en el sistema.", fontPersonaInfo);
            vacio.setAlignment(Element.ALIGN_CENTER);
            document.add(vacio);
        } else {
            int personaIndex = 1;
            for (Persona persona : personas) {
                PersonaDto personaDto = modelMapper.map(persona, PersonaDto.class);
                List<Prestamo> prestamosPersona = prestamoRepository.findByPersonaIdOrderByFechaPrestamoDesc(persona.getId());

                // Encabezado de la persona
                PdfPTable infoPersonaTable = new PdfPTable(1);
                infoPersonaTable.setWidthPercentage(100);
                infoPersonaTable.setSpacingBefore(10);

                String domicilioStr = "-";
                if (personaDto.getDomicilio() != null) {
                    String calle = personaDto.getDomicilio().getCalle() != null ? personaDto.getDomicilio().getCalle() : "";
                    String num = personaDto.getDomicilio().getNumero() > 0 ? String.valueOf(personaDto.getDomicilio().getNumero()) : "";
                    String loc = (personaDto.getDomicilio().getLocalidad() != null && personaDto.getDomicilio().getLocalidad().getDenominacion() != null)
                            ? " (" + personaDto.getDomicilio().getLocalidad().getDenominacion() + ")"
                            : "";
                    domicilioStr = (calle + " " + num).trim() + loc;
                }

                String dniStr = personaDto.getDni() > 0 ? String.valueOf(personaDto.getDni()) : "-";
                String textoPersona = String.format("%d. %s %s | DNI: %s | Domicilio: %s | Total Préstamos: %d",
                        personaIndex++,
                        personaDto.getNombre() != null ? personaDto.getNombre() : "",
                        personaDto.getApellido() != null ? personaDto.getApellido() : "",
                        dniStr,
                        domicilioStr,
                        prestamosPersona.size()
                );

                PdfPCell cellPersona = new PdfPCell(new Phrase(textoPersona, fontPersonaHeader));
                cellPersona.setBackgroundColor(new BaseColor(240, 244, 248));
                cellPersona.setBorderColor(new BaseColor(200, 215, 230));
                cellPersona.setPadding(6);
                infoPersonaTable.addCell(cellPersona);
                document.add(infoPersonaTable);

                // Tabla de préstamos de la persona
                PdfPTable tablePrestamos = new PdfPTable(new float[]{1.0f, 4.0f, 2.0f, 2.0f, 2.0f});
                tablePrestamos.setWidthPercentage(100);
                tablePrestamos.setSpacingAfter(10);

                // Cabeceras de tabla
                String[] headers = {"#", "Libro Solicitado", "Fecha Préstamo", "Fecha Devolución", "Estado"};
                for (String h : headers) {
                    PdfPCell headerCell = new PdfPCell(new Phrase(h, fontTableHeader));
                    headerCell.setBackgroundColor(new BaseColor(13, 110, 253));
                    headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    headerCell.setPadding(5);
                    tablePrestamos.addCell(headerCell);
                }

                if (prestamosPersona.isEmpty()) {
                    PdfPCell emptyCell = new PdfPCell(new Phrase("Sin registros detallados de préstamos.", fontTableBody));
                    emptyCell.setColspan(5);
                    emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    emptyCell.setPadding(6);
                    tablePrestamos.addCell(emptyCell);
                } else {
                    int prestamoNum = 1;
                    for (Prestamo p : prestamosPersona) {
                        PrestamoDto pDto = modelMapper.map(p, PrestamoDto.class);
                        BaseColor rowColor = (prestamoNum % 2 == 0) ? new BaseColor(248, 249, 250) : BaseColor.WHITE;

                        // Col 0: Index
                        PdfPCell c0 = new PdfPCell(new Phrase(String.valueOf(prestamoNum++), fontTableBody));
                        c0.setBackgroundColor(rowColor);
                        c0.setHorizontalAlignment(Element.ALIGN_CENTER);
                        c0.setPadding(4);
                        tablePrestamos.addCell(c0);

                        // Col 1: Libro
                        String tituloLibro = (pDto.getLibro() != null && pDto.getLibro().getTitulo() != null)
                                ? pDto.getLibro().getTitulo() : "-";
                        PdfPCell c1 = new PdfPCell(new Phrase(tituloLibro, fontTableBody));
                        c1.setBackgroundColor(rowColor);
                        c1.setPadding(4);
                        tablePrestamos.addCell(c1);

                        // Col 2: Fecha Préstamo
                        String fPrestamo = pDto.getFechaPrestamo() != null ? pDto.getFechaPrestamo().toString() : "-";
                        PdfPCell c2 = new PdfPCell(new Phrase(fPrestamo, fontTableBody));
                        c2.setBackgroundColor(rowColor);
                        c2.setHorizontalAlignment(Element.ALIGN_CENTER);
                        c2.setPadding(4);
                        tablePrestamos.addCell(c2);

                        // Col 3: Fecha Devolución
                        String fDevolucion = pDto.getFechaDevolucion() != null ? pDto.getFechaDevolucion().toString() : "-";
                        PdfPCell c3 = new PdfPCell(new Phrase(fDevolucion, fontTableBody));
                        c3.setBackgroundColor(rowColor);
                        c3.setHorizontalAlignment(Element.ALIGN_CENTER);
                        c3.setPadding(4);
                        tablePrestamos.addCell(c3);

                        // Col 4: Estado
                        String estado = pDto.getEstado() != null ? pDto.getEstado() : "-";
                        com.itextpdf.text.Font fontEstado = "ACTIVO".equalsIgnoreCase(estado) ? fontBadgeActivo : fontBadgeDevuelto;
                        PdfPCell c4 = new PdfPCell(new Phrase(estado, fontEstado));
                        c4.setBackgroundColor(rowColor);
                        c4.setHorizontalAlignment(Element.ALIGN_CENTER);
                        c4.setPadding(4);
                        tablePrestamos.addCell(c4);
                    }
                }

                document.add(tablePrestamos);
            }
        }

        document.close();
        return out.toByteArray();
    }

    @Override
    public byte[] generarExcelLibrosDisponibles() throws Exception {
        List<LibroDto> disponibles = libroService.findDisponibles();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Libros Disponibles");

            // Estilos
            CellStyle titleStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleFont.setColor(IndexedColors.DARK_GREEN.getIndex());
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle subtitleStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font subtitleFont = workbook.createFont();
            subtitleFont.setFontHeightInPoints((short) 10);
            subtitleFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            subtitleStyle.setFont(subtitleFont);
            subtitleStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle dataCenterStyle = workbook.createCellStyle();
            dataCenterStyle.cloneStyleFrom(dataStyle);
            dataCenterStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle badgeDisponibleStyle = workbook.createCellStyle();
            badgeDisponibleStyle.cloneStyleFrom(dataCenterStyle);
            org.apache.poi.ss.usermodel.Font dispFont = workbook.createFont();
            dispFont.setBold(true);
            dispFont.setColor(IndexedColors.DARK_GREEN.getIndex());
            badgeDisponibleStyle.setFont(dispFont);

            // Fila 0: Título
            Row rowTitle = sheet.createRow(0);
            Cell cellTitle = rowTitle.createCell(0);
            cellTitle.setCellValue("REPORTE DE LIBROS DISPONIBLES PARA PRÉSTAMO");
            cellTitle.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

            // Fila 1: Subtítulo
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String fechaGen = LocalDateTime.now().format(dtf);
            Row rowSub = sheet.createRow(1);
            Cell cellSub = rowSub.createCell(0);
            cellSub.setCellValue("Generado el: " + fechaGen + " | Total de libros disponibles: " + disponibles.size());
            cellSub.setCellStyle(subtitleStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 5));

            // Fila 3: Encabezados de tabla
            String[] headers = {"ID Libro", "Título", "Género", "Año / Fecha", "Páginas", "Disponibilidad"};
            Row headerRow = sheet.createRow(3);
            headerRow.setHeightInPoints(25);
            for (int i = 0; i < headers.length; i++) {
                Cell c = headerRow.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            // Filas de datos
            int rowIdx = 4;
            for (LibroDto libro : disponibles) {
                Row row = sheet.createRow(rowIdx++);

                Cell cId = row.createCell(0);
                cId.setCellValue(libro.getId() != null ? libro.getId() : 0);
                cId.setCellStyle(dataCenterStyle);

                Cell cTitulo = row.createCell(1);
                cTitulo.setCellValue(libro.getTitulo() != null ? libro.getTitulo() : "-");
                cTitulo.setCellStyle(dataStyle);

                Cell cGenero = row.createCell(2);
                cGenero.setCellValue(libro.getGenero() != null ? libro.getGenero() : "-");
                cGenero.setCellStyle(dataCenterStyle);

                Cell cFecha = row.createCell(3);
                cFecha.setCellValue(libro.getFecha() > 0 ? String.valueOf(libro.getFecha()) : "-");
                cFecha.setCellStyle(dataCenterStyle);

                Cell cPaginas = row.createCell(4);
                cPaginas.setCellValue(libro.getPaginas() > 0 ? String.valueOf(libro.getPaginas()) : "-");
                cPaginas.setCellStyle(dataCenterStyle);

                Cell cDisp = row.createCell(5);
                cDisp.setCellValue("Disponible");
                cDisp.setCellStyle(badgeDisponibleStyle);
            }

            // Auto-ajustar ancho de columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, Math.max(sheet.getColumnWidth(i) + 1200, 3000));
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}

