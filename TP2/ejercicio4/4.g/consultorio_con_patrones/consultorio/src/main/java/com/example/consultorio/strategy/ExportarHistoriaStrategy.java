package com.example.consultorio.strategy;

import com.example.consultorio.entities.HistoriaClinica;

// Interfaz estrategia:
// Es util aplicar el patron Strategy para la exportacion de historias clinicas porque existen
// multiples algoritmos/formatos posibles para exportar la misma informacion (TXT y en el
// futuro PDF, Excel, JSON, etc).
// En lugar de llenar el servicio o la fachada con estructuras condicionales (if/else o switch)
// acopladas a cada formato, esta interfaz define un contrato comun para que todas las estrategias
// de exportacion sean intercambiables en tiempo de ejecucion, respetando el principio Abierto/Cerrado.
public interface ExportarHistoriaStrategy {

    // Ejecuta el algoritmo especifico de formateo y devuelve los bytes del archivo generado
    byte[] exportar(HistoriaClinica historia);

    // Identificador del formato (ej. "txt", "csv")
    String getFormato();

    // Extension del archivo descargable (ej. ".txt", ".csv")
    String getExtension();

    // Tipo MIME para la respuesta HTTP
    String getContentType();
}

