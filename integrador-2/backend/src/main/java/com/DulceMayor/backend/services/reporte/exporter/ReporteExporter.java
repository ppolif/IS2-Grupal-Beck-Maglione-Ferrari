package com.DulceMayor.backend.services.reporte.exporter;

import com.DulceMayor.backend.dtos.reporte.ReporteTabular;

/**
 * Interfaz común del producto (Product del patrón Factory Method) para los exportadores
 * binarios de un {@link ReporteTabular}.
 *
 * <p><b>Patrón GoF aplicado: Factory Method (Creacional)</b></p>
 * <ul>
 *   <li><b>Participante GoF:</b> Product (Interfaz de los objetos creados por el método de fábrica).</li>
 *   <li><b>Justificación:</b> Permite que el motor de reportes exporte cualquier {@link ReporteTabular}
 *       a distintos formatos documentales (PDF, Excel XLSX) bajo un mismo contrato polimórfico.</li>
 * </ul>
 */
public interface ReporteExporter {

    /**
     * Código identificador del formato (por ejemplo, {@code "pdf"} o {@code "xlsx"}).
     */
    String getFormato();

    /**
     * MIME Content-Type estándar correspondiente al archivo generado.
     */
    String getContentType();

    /**
     * Extensión de archivo para el encabezado {@code Content-Disposition}.
     */
    String getExtensionArchivo();

    /**
     * Serializa el {@link ReporteTabular} al formato binario correspondiente.
     */
    byte[] exportar(ReporteTabular reporte);
}

