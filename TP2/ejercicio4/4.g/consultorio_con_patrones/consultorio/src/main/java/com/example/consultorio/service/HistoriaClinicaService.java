package com.example.consultorio.service;

import com.example.consultorio.entities.DetalleHistoriaClinica;
import com.example.consultorio.entities.HistoriaClinica;
import com.example.consultorio.entities.Paciente;
import com.example.consultorio.entities.Usuario;
import com.example.consultorio.repositories.DetalleHistoriaClinicaRepository;
import com.example.consultorio.repositories.HistoriaClinicaRepository;
import com.example.consultorio.repositories.PacienteRepository;
import com.example.consultorio.template.TemplateGestorHistoria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class HistoriaClinicaService extends TemplateGestorHistoria {

    @Autowired
    private HistoriaClinicaRepository historiaClinicaRepository;

    @Autowired
    private DetalleHistoriaClinicaRepository detalleRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    ///a continuacion se sobreescriben todos los metodos necesarios que provienen de la clase plantilla
    @Override
    protected Paciente obtenerPacienteActivo(String idPaciente) {
        return pacienteRepository.findByIdAndEliminadoFalse(idPaciente)
                .or(() -> pacienteRepository.findByDniAndEliminadoFalse(idPaciente))
                .orElseThrow(() -> new RuntimeException("El paciente no existe o se encuentra inactivo."));
    }

    @Override
    protected void verificarHistoriaInexistente(Paciente paciente) {
        if (historiaClinicaRepository.existsByPacienteDniAndEliminadoFalse(paciente.getDni())) {
            throw new IllegalStateException("El paciente ya posee una historia clínica activa en el consultorio.");
        }
    }

    // Usamos el Builder de HistoriaClinica para construir la instancia inicial de forma fluida,
    // delegando al Builder la inicializacion por defecto de su coleccion de detalles y estado.
    @Override
    protected HistoriaClinica inicializarHistoria(Paciente paciente, Usuario usuario) {
        return HistoriaClinica.builder()
                .paciente(paciente)
                .usuario(usuario)
                .eliminado(false)
                .build();
    }

    // Usamos el Builder de DetalleHistoriaClinica para construir un detalle consistente
    // con su fecha actual, medico, descripcion y referencia a la historia clinica antes de asociarlo.
    @Override
    protected void asociarDetalleAHistoria(HistoriaClinica historia, DetalleHistoriaClinica detalle) {
        if (historia.getDetalles() == null) {
            historia.setDetalles(new java.util.ArrayList<>());
        }
        DetalleHistoriaClinica nuevoDetalle = DetalleHistoriaClinica.builder()
                .id(null)
                .fechaHistoria(new Date())
                .detalleHistoria(detalle.getDetalleHistoria())
                .medico(detalle.getMedico())
                .historia(historia)
                .eliminado(false)
                .build();
        historia.getDetalles().add(nuevoDetalle);
    }

    @Override
    @Transactional
    protected void persistirHistoria(HistoriaClinica historia) {
        historiaClinicaRepository.save(historia);
    }

    @Override
    public HistoriaClinica obtenerHistoriaDelPaciente(Paciente paciente) {
        return historiaClinicaRepository.findByPacienteDniAndEliminadoFalse(paciente.getDni())
                .orElseThrow(() -> new RuntimeException("No se encontró historia clínica para el paciente indicado."));
    }

    @Override
    public HistoriaClinica obtenerHistoriaPorId(String idHistoria) {
        return historiaClinicaRepository.findByIdAndEliminadoFalse(idHistoria)
                .orElseThrow(() -> new RuntimeException("Historia clínica no encontrada"));
    }

    @Override
    protected void marcarDetalleEliminado(HistoriaClinica historia, String idDetalle) {
        boolean encontrado = false;
        for (DetalleHistoriaClinica det : historia.getDetalles()) {
            if (det.getId().equals(idDetalle)) {
                det.setEliminado(true);
                encontrado = true;
                break;
            }
        }
        if (!encontrado) {
            throw new RuntimeException("Detalle no encontrado en la historia clínica.");
        }
    }


    @Transactional(readOnly = true)
    public List<HistoriaClinica> listarHistoriasActivas() {
        return historiaClinicaRepository.findByEliminadoFalse();
    }

}
