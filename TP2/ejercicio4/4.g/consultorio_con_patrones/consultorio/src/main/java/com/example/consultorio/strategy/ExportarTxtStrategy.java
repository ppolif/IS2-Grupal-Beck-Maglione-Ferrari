package com.example.consultorio.strategy;

import com.example.consultorio.entities.DetalleHistoriaClinica;
import com.example.consultorio.entities.HistoriaClinica;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;

// estrategia concreta (exportacion en formato TXT):
// Implementa el algoritmo especifico para transformar una HistoriaClinica y sus detalles activos
// en un informe legible de texto plano (.txt). Si cambia el diseno del informe TXT, solo se
// modifica esta clase sin afectar al resto del sistema ni a otros formatos de exportacion.
@Component
public class ExportarTxtStrategy implements ExportarHistoriaStrategy {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    @Override
    public byte[] exportar(HistoriaClinica historia) {
        StringBuilder sb = new StringBuilder();
        sb.append("=================================================================\n");
        sb.append("                        HISTORIA CLINICA                         \n");
        sb.append("=================================================================\n");
        sb.append("ID Historia : ").append(historia.getId()).append("\n");
        if (historia.getPaciente() != null) {
            sb.append("Paciente    : ").append(historia.getPaciente().getApellido())
              .append(", ").append(historia.getPaciente().getNombre()).append("\n");
            sb.append("DNI         : ").append(historia.getPaciente().getDni()).append("\n");
        }
        sb.append("Abierta por : ").append(historia.getUsuario() != null ? historia.getUsuario().getNombreUsuario() : "Sistema").append("\n");
        sb.append("=================================================================\n\n");
        sb.append("EVOLUCION Y CONSULTAS MEDICAS:\n");
        sb.append("-----------------------------------------------------------------\n");

        boolean tieneDetallesActivos = false;
        if (historia.getDetalles() != null) {
            for (DetalleHistoriaClinica det : historia.getDetalles()) {
                if (!det.isEliminado()) {
                    tieneDetallesActivos = true;
                    String fecha = det.getFechaHistoria() != null ? DATE_FORMAT.format(det.getFechaHistoria()) : "Sin fecha";
                    String medico = det.getMedico() != null
                            ? det.getMedico().getApellido() + ", " + det.getMedico().getNombre() + " (DNI: " + det.getMedico().getDni() + ")"
                            : "No especificado";

                    sb.append("Fecha  : ").append(fecha).append("\n");
                    sb.append("Medico : Dr/a. ").append(medico).append("\n");
                    sb.append("Detalle:\n").append(det.getDetalleHistoria() != null ? det.getDetalleHistoria() : "").append("\n");
                    sb.append("-----------------------------------------------------------------\n");
                }
            }
        }

        if (!tieneDetallesActivos) {
            sb.append("No se registran detalles clinicos activos.\n");
            sb.append("-----------------------------------------------------------------\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String getFormato() {
        return "txt";
    }

    @Override
    public String getExtension() {
        return ".txt";
    }

    @Override
    public String getContentType() {
        return "text/plain; charset=UTF-8";
    }
}

