package com.example.zero.services;

import com.example.zero.dto.reporte.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Servicio encargado de generar archivos de exportación en formato .xlsx (Excel) y .csv
 * a partir de los DTOs de reportes.
 */
@Service
public class ReporteExportService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // =========================================================================
    // EXPORTACIÓN REPORTE DE VENTAS
    // =========================================================================

    public byte[] exportarVentasCsv(ReporteVentasDTO dto) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {
            // Escribir BOM UTF-8 para apertura directa en Excel
            baos.write(0xEF);
            baos.write(0xBB);
            baos.write(0xBF);

            // Resumen superior
            writer.println("REPORTE DE VENTAS");
            writer.println("Período;" + (dto.getFechaDesde() != null ? dto.getFechaDesde().format(DATE_FORMATTER) : "") + " al " +
                    (dto.getFechaHasta() != null ? dto.getFechaHasta().format(DATE_FORMATTER) : ""));
            writer.println("Total Recaudado ($);" + dto.getTotalRecaudado());
            writer.println("Cantidad de Ventas;" + dto.getCantidadVentas());
            writer.println("Total Artículos Vendidos;" + dto.getTotalArticulosVendidos());
            writer.println("Ticket Promedio ($);" + dto.getTicketPromedio());
            writer.println();

            // Encabezados de tabla (sin Estado)
            writer.println("N° Compra;Fecha;Código Producto;Producto;Categoría;Cantidad;Precio Unitario ($);Subtotal ($);Forma de Pago;Cliente;Identificador Cliente");

            if (dto.getItems() != null) {
                for (ReporteVentaItemDTO item : dto.getItems()) {
                    writer.println(String.format("%s;%s;%s;\"%s\";\"%s\";%d;%.2f;%.2f;\"%s\";\"%s\";\"%s\"",
                            csvEscape(item.getNumeroOrden()),
                            item.getFechaCompra() != null ? item.getFechaCompra().format(DATE_TIME_FORMATTER) : "",
                            csvEscape(item.getProductoCodigo()),
                            csvEscape(item.getProductoNombre()),
                            csvEscape(item.getCategoriaNombre()),
                            item.getCantidad(),
                            item.getPrecioUnitario(),
                            item.getSubtotal(),
                            csvEscape(item.getFormaPago()),
                            csvEscape(item.getClienteNombre()),
                            csvEscape(item.getClienteIdentificador())
                    ));
                }
            }
            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar CSV de ventas", e);
        }
        return baos.toByteArray();
    }

    public byte[] exportarVentasXlsx(ReporteVentasDTO dto) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Reporte Ventas");

            CellStyle headerStyle = crearHeaderStyle(workbook);
            CellStyle currencyStyle = crearCurrencyStyle(workbook);

            // Resumen superior
            int rowIdx = 0;
            Row r0 = sheet.createRow(rowIdx++);
            r0.createCell(0).setCellValue("REPORTE DE VENTAS");
            Row r1 = sheet.createRow(rowIdx++);
            r1.createCell(0).setCellValue("Período:");
            r1.createCell(1).setCellValue((dto.getFechaDesde() != null ? dto.getFechaDesde().format(DATE_FORMATTER) : "") + " al " +
                    (dto.getFechaHasta() != null ? dto.getFechaHasta().format(DATE_FORMATTER) : ""));
            Row r2 = sheet.createRow(rowIdx++);
            r2.createCell(0).setCellValue("Total Recaudado ($):");
            Cell cRec = r2.createCell(1);
            cRec.setCellValue(dto.getTotalRecaudado());
            cRec.setCellStyle(currencyStyle);

            Row r3 = sheet.createRow(rowIdx++);
            r3.createCell(0).setCellValue("Cantidad de Ventas:");
            r3.createCell(1).setCellValue(dto.getCantidadVentas());

            Row r4 = sheet.createRow(rowIdx++);
            r4.createCell(0).setCellValue("Total Artículos Vendidos:");
            r4.createCell(1).setCellValue(dto.getTotalArticulosVendidos());

            Row r5 = sheet.createRow(rowIdx++);
            r5.createCell(0).setCellValue("Ticket Promedio ($):");
            Cell cTick = r5.createCell(1);
            cTick.setCellValue(dto.getTicketPromedio());
            cTick.setCellStyle(currencyStyle);

            rowIdx++; // Espacio en blanco

            // Encabezados de tabla
            Row headerRow = sheet.createRow(rowIdx++);
            String[] headers = {
                    "N° Compra", "Fecha Compra", "Código", "Producto", "Categoría",
                    "Cantidad", "Precio Unitario", "Subtotal", "Forma de Pago", "Cliente", "Identificador Cliente"
            };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Datos
            if (dto.getItems() != null) {
                for (ReporteVentaItemDTO item : dto.getItems()) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(item.getNumeroOrden() != null ? item.getNumeroOrden() : "");
                    row.createCell(1).setCellValue(item.getFechaCompra() != null ? item.getFechaCompra().format(DATE_TIME_FORMATTER) : "");
                    row.createCell(2).setCellValue(item.getProductoCodigo() != null ? item.getProductoCodigo() : "");
                    row.createCell(3).setCellValue(item.getProductoNombre() != null ? item.getProductoNombre() : "");
                    row.createCell(4).setCellValue(item.getCategoriaNombre() != null ? item.getCategoriaNombre() : "");
                    row.createCell(5).setCellValue(item.getCantidad());

                    Cell cUnit = row.createCell(6);
                    cUnit.setCellValue(item.getPrecioUnitario());
                    cUnit.setCellStyle(currencyStyle);

                    Cell cSub = row.createCell(7);
                    cSub.setCellValue(item.getSubtotal());
                    cSub.setCellStyle(currencyStyle);

                    row.createCell(8).setCellValue(item.getFormaPago() != null ? item.getFormaPago() : "");
                    row.createCell(9).setCellValue(item.getClienteNombre() != null ? item.getClienteNombre() : "");
                    row.createCell(10).setCellValue(item.getClienteIdentificador() != null ? item.getClienteIdentificador() : "");
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error al generar Excel de ventas", e);
        }
    }

    // =========================================================================
    // EXPORTACIÓN REPORTE DE PRODUCTOS (STOCK)
    // =========================================================================

    public byte[] exportarProductosCsv(ReporteProductosDTO dto) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {
            baos.write(0xEF);
            baos.write(0xBB);
            baos.write(0xBF);

            writer.println("REPORTE DE PRODUCTOS Y STOCK");
            writer.println("Sucursal;" + (dto.getSucursalNombre() != null ? dto.getSucursalNombre() : "Central"));
            writer.println("Total Stock Unidades;" + dto.getTotalStockUnidades());
            writer.println("Stock Bien (> 50);" + dto.getStockBienCount());
            writer.println("Stock Regular (20 a 50);" + dto.getStockRegularCount());
            writer.println("Stock Malo (< 20);" + dto.getStockMaloCount());
            writer.println();

            writer.println("Código;Producto;Categoría;Talle;Sucursal;Stock Actual;Estado Stock;Faltante para 50%;Proveedor Menor Costo;Menor Costo ($);Teléfono Proveedor");

            if (dto.getProductos() != null) {
                for (ReporteProductoStockDTO p : dto.getProductos()) {
                    writer.println(String.format("%s;\"%s\";\"%s\";\"%s\";\"%s\";%d;\"%s\";%d;\"%s\";%s;\"%s\"",
                            csvEscape(p.getCodigo()),
                            csvEscape(p.getNombre()),
                            csvEscape(p.getCategoria()),
                            csvEscape(p.getTalle()),
                            csvEscape(p.getSucursal()),
                            p.getStockActual(),
                            csvEscape(p.getEstadoStock()),
                            p.getCantidadFaltante(),
                            csvEscape(p.getProveedorNombre()),
                            p.getMenorCosto() != null ? String.format("%.2f", p.getMenorCosto()) : "N/A",
                            csvEscape(p.getProveedorTelefono() != null ? p.getProveedorTelefono() : "N/A")
                    ));
                }
            }
            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar CSV de productos", e);
        }
        return baos.toByteArray();
    }

    public byte[] exportarProductosXlsx(ReporteProductosDTO dto) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Reporte Productos");

            CellStyle headerStyle = crearHeaderStyle(workbook);
            CellStyle currencyStyle = crearCurrencyStyle(workbook);

            int rowIdx = 0;
            Row r0 = sheet.createRow(rowIdx++);
            r0.createCell(0).setCellValue("REPORTE DE PRODUCTOS Y CONTROL DE STOCK");
            Row r1 = sheet.createRow(rowIdx++);
            r1.createCell(0).setCellValue("Sucursal:");
            r1.createCell(1).setCellValue(dto.getSucursalNombre() != null ? dto.getSucursalNombre() : "Central");
            Row r2 = sheet.createRow(rowIdx++);
            r2.createCell(0).setCellValue("Total Stock Unidades:");
            r2.createCell(1).setCellValue(dto.getTotalStockUnidades());
            Row r3 = sheet.createRow(rowIdx++);
            r3.createCell(0).setCellValue("Stock Bien (> 50):");
            r3.createCell(1).setCellValue(dto.getStockBienCount());
            Row r4 = sheet.createRow(rowIdx++);
            r4.createCell(0).setCellValue("Stock Regular (20 - 50):");
            r4.createCell(1).setCellValue(dto.getStockRegularCount());
            Row r5 = sheet.createRow(rowIdx++);
            r5.createCell(0).setCellValue("Stock Malo (< 20):");
            r5.createCell(1).setCellValue(dto.getStockMaloCount());

            rowIdx++; // Espacio

            Row headerRow = sheet.createRow(rowIdx++);
            String[] headers = {
                    "Código", "Producto", "Categoría", "Talle", "Sucursal",
                    "Stock Actual", "Estado Stock", "Faltante para 50%", "Proveedor Menor Costo", "Menor Costo ($)", "Teléfono Proveedor"
            };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            if (dto.getProductos() != null) {
                for (ReporteProductoStockDTO p : dto.getProductos()) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(p.getCodigo() != null ? p.getCodigo() : "");
                    row.createCell(1).setCellValue(p.getNombre() != null ? p.getNombre() : "");
                    row.createCell(2).setCellValue(p.getCategoria() != null ? p.getCategoria() : "");
                    row.createCell(3).setCellValue(p.getTalle() != null ? p.getTalle() : "");
                    row.createCell(4).setCellValue(p.getSucursal() != null ? p.getSucursal() : "");
                    row.createCell(5).setCellValue(p.getStockActual());
                    row.createCell(6).setCellValue(p.getEstadoStock() != null ? p.getEstadoStock() : "");
                    row.createCell(7).setCellValue(p.getCantidadFaltante());
                    row.createCell(8).setCellValue(p.getProveedorNombre() != null ? p.getProveedorNombre() : "");

                    Cell cCosto = row.createCell(9);
                    if (p.getMenorCosto() != null) {
                        cCosto.setCellValue(p.getMenorCosto());
                        cCosto.setCellStyle(currencyStyle);
                    } else {
                        cCosto.setCellValue("N/A");
                    }

                    row.createCell(10).setCellValue(p.getProveedorTelefono() != null ? p.getProveedorTelefono() : "N/A");
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error al generar Excel de productos", e);
        }
    }

    // =========================================================================
    // EXPORTACIÓN REPORTE DE PROVEEDORES (MENOR COSTO)
    // =========================================================================

    public byte[] exportarProveedoresCsv(ReporteProveedoresDTO dto) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {
            baos.write(0xEF);
            baos.write(0xBB);
            baos.write(0xBF);

            writer.println("REPORTE DE PROVEEDORES - MENOR PRECIO DE COSTO POR PRODUCTO");
            writer.println("Total Productos en Catálogo;" + dto.getTotalProductos());
            writer.println("Proveedor Más Conveniente;" + (dto.getProveedorMasConveniente() != null ? dto.getProveedorMasConveniente() : "N/A"));
            writer.println("Productos al Menor Costo con Proveedor Líder;" + dto.getProductosConMejorPrecioProveedor());
            writer.println();

            // Sin columna Categoría por pedido del usuario
            writer.println("Código;Producto;Proveedor Más Económico;CUIT;Menor Costo ($);Factura Referencia;Fecha Compra;Teléfono Contacto");

            if (dto.getItems() != null) {
                for (ReporteProveedorPrecioDTO item : dto.getItems()) {
                    String refFactura = (item.getNumeroFacturaReferencia() != null) ? "#ORD-" + item.getNumeroFacturaReferencia() : "N/A";
                    String fecha = (item.getFechaFacturaReferencia() != null) ? item.getFechaFacturaReferencia().format(DATE_FORMATTER) : "N/A";

                    writer.println(String.format("%s;\"%s\";\"%s\";\"%s\";%s;\"%s\";\"%s\";\"%s\"",
                            csvEscape(item.getProductoCodigo()),
                            csvEscape(item.getProductoNombre()),
                            csvEscape(item.getProveedorRazonSocial()),
                            csvEscape(item.getProveedorCuit()),
                            item.getMenorPrecioCosto() != null ? String.format("%.2f", item.getMenorPrecioCosto()) : "N/A",
                            csvEscape(refFactura),
                            csvEscape(fecha),
                            csvEscape(item.getProveedorTelefono() != null ? item.getProveedorTelefono() : "N/A")
                    ));
                }
            }
            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar CSV de proveedores", e);
        }
        return baos.toByteArray();
    }

    public byte[] exportarProveedoresXlsx(ReporteProveedoresDTO dto) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Reporte Proveedores");

            CellStyle headerStyle = crearHeaderStyle(workbook);
            CellStyle currencyStyle = crearCurrencyStyle(workbook);

            int rowIdx = 0;
            Row r0 = sheet.createRow(rowIdx++);
            r0.createCell(0).setCellValue("REPORTE DE PROVEEDORES - MENOR PRECIO DE COSTO POR PRODUCTO");
            Row r1 = sheet.createRow(rowIdx++);
            r1.createCell(0).setCellValue("Total Productos en Catálogo:");
            r1.createCell(1).setCellValue(dto.getTotalProductos());
            Row r2 = sheet.createRow(rowIdx++);
            r2.createCell(0).setCellValue("Proveedor Más Conveniente:");
            r2.createCell(1).setCellValue(dto.getProveedorMasConveniente() != null ? dto.getProveedorMasConveniente() : "N/A");
            Row r3 = sheet.createRow(rowIdx++);
            r3.createCell(0).setCellValue("Productos con Mejor Costo del Líder:");
            r3.createCell(1).setCellValue(dto.getProductosConMejorPrecioProveedor());

            rowIdx++; // Espacio

            // Sin Categoría
            Row headerRow = sheet.createRow(rowIdx++);
            String[] headers = {
                    "Código", "Producto", "Proveedor Más Económico", "CUIT Proveedor",
                    "Menor Costo ($)", "Factura Referencia", "Fecha Compra", "Teléfono Contacto"
            };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            if (dto.getItems() != null) {
                for (ReporteProveedorPrecioDTO item : dto.getItems()) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(item.getProductoCodigo() != null ? item.getProductoCodigo() : "");
                    row.createCell(1).setCellValue(item.getProductoNombre() != null ? item.getProductoNombre() : "");
                    row.createCell(2).setCellValue(item.getProveedorRazonSocial() != null ? item.getProveedorRazonSocial() : "");
                    row.createCell(3).setCellValue(item.getProveedorCuit() != null ? item.getProveedorCuit() : "");

                    Cell cCosto = row.createCell(4);
                    if (item.getMenorPrecioCosto() != null) {
                        cCosto.setCellValue(item.getMenorPrecioCosto());
                        cCosto.setCellStyle(currencyStyle);
                    } else {
                        cCosto.setCellValue("N/A");
                    }

                    String refFactura = (item.getNumeroFacturaReferencia() != null) ? "#ORD-" + item.getNumeroFacturaReferencia() : "N/A";
                    String fecha = (item.getFechaFacturaReferencia() != null) ? item.getFechaFacturaReferencia().format(DATE_FORMATTER) : "N/A";

                    row.createCell(5).setCellValue(refFactura);
                    row.createCell(6).setCellValue(fecha);
                    row.createCell(7).setCellValue(item.getProveedorTelefono() != null ? item.getProveedorTelefono() : "N/A");
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error al generar Excel de proveedores", e);
        }
    }

    // =========================================================================
    // UTILIDADES DE FORMATO Y ESTILO
    // =========================================================================

    private CellStyle crearHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle crearCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("$#,##0.00"));
        return style;
    }

    private String csvEscape(String valor) {
        if (valor == null) return "";
        return valor.replace("\"", "\"\"").replace("\n", " ").replace("\r", " ").trim();
    }
}
