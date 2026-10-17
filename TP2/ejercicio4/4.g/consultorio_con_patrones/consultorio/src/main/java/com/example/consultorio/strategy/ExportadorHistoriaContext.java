package com.example.consultorio.strategy;

import com.example.consultorio.entities.HistoriaClinica;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// contexto:
// Esta clase actua como el Contexto del patron Strategy: mantiene una referencia a una
// estrategia de exportacion (ExportarHistoriaStrategy) y permite cambiarla dinamicamente
// segun el formato solicitado ("txt", "csv") sin acoplarse a la implementacion concreta de ninguna.
// Gracias a la inyeccion de dependencias de Spring, registra automaticamente todas las estrategias
// que implementen ExportarHistoriaStrategy.
@Component
public class ExportadorHistoriaContext {

    private final Map<String, ExportarHistoriaStrategy> estrategias = new HashMap<>();
    private ExportarHistoriaStrategy strategy;

    public ExportadorHistoriaContext(List<ExportarHistoriaStrategy> listaEstrategias) {
        for (ExportarHistoriaStrategy est : listaEstrategias) {
            estrategias.put(est.getFormato().toLowerCase(), est);
        }
    }

    public void setStrategy(ExportarHistoriaStrategy strategy) {
        this.strategy = strategy;
    }

    public ExportarHistoriaStrategy seleccionarEstrategia(String formato) {
        if (formato == null || formato.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe indicar un formato de exportación.");
        }
        ExportarHistoriaStrategy seleccionada = estrategias.get(formato.trim().toLowerCase());
        if (seleccionada == null) {
            throw new IllegalArgumentException("Formato de exportación no soportado: " + formato);
        }
        this.strategy = seleccionada;
        return seleccionada;
    }

    public byte[] ejecutarExportacion(HistoriaClinica historia) {
        if (this.strategy == null) {
            throw new IllegalStateException("No se ha seleccionado ninguna estrategia de exportación.");
        }
        return this.strategy.exportar(historia);
    }
}

