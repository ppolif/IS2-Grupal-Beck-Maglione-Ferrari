package com.example.consultorio.facade;

import com.example.consultorio.entities.DetalleHistoriaClinica;
import com.example.consultorio.entities.HistoriaClinica;
import com.example.consultorio.entities.Medico;
import com.example.consultorio.entities.Paciente;
import com.example.consultorio.service.HistoriaClinicaService;
import com.example.consultorio.service.MedicoService;
import com.example.consultorio.service.PacienteService;
import com.example.consultorio.service.UsuarioService;
import com.example.consultorio.strategy.ExportadorHistoriaContext;
import com.example.consultorio.strategy.ExportarHistoriaStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

// La HistoriaClinica es el nucleo del sistema y actua como punto de convergencia de multiples
// clases: Paciente (con su FotoPaciente), Usuario, DetalleHistoriaClinica y Medico.
// Sin esta fachada, el controlador web tendria que inyectar y coordinar directamente
// PacienteService, MedicoService, UsuarioService e HistoriaClinicaService, buscar cada entidad,
// instanciar objetos con sus Builders y armar las relaciones antes de guardar.
//
// Al introducir ConsultorioFacade, ofrecemos una interfaz unica y simplificada hacia la capa web:
// el controlador solo pasa los IDs y el texto recibidos del formulario en una unica llamada,
// y la fachada se encarga de orquestar todos los servicios y construir los objetos necesarios.
@Component
public class ConsultorioFacade {

    @Autowired
    private HistoriaClinicaService historiaClinicaService;

    @Autowired
    private PacienteService pacienteService;

    @Autowired
    private MedicoService medicoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ExportadorHistoriaContext exportadorHistoriaContext;

    // Metodo de fachada que unifica la apertura de una historia clinica:
    // busca al medico, construye el primer DetalleHistoriaClinica usando el patron Builder
    // y delega el flujo de validacion, creacion y persistencia al servicio de historia clinica.
    public void registrarHistoriaClinica(String dniPaciente, String medicoId, String textoDetalle) {
        Medico medico = medicoService.buscarPorId(medicoId);

        DetalleHistoriaClinica primerDetalle = DetalleHistoriaClinica.builder()
                .detalleHistoria(textoDetalle)
                .medico(medico)
                .build();

        historiaClinicaService.ejecutarRegistro(dniPaciente, primerDetalle);
    }

    // Metodo de fachada que unifica el agregado de una nueva evolucion medica a una historia existente:
    // recupera la historia y el medico involucrado, construye el objeto DetalleHistoriaClinica
    // con su Builder y ejecuta el metodo plantilla para asociarlo y persistirlo.
    public void agregarDetalleAHistoria(String idHistoria, String medicoId, String textoDetalle) {
        HistoriaClinica historia = historiaClinicaService.obtenerHistoriaPorId(idHistoria);
        Medico medico = medicoService.buscarPorId(medicoId);

        DetalleHistoriaClinica nuevoDetalle = DetalleHistoriaClinica.builder()
                .detalleHistoria(textoDetalle)
                .medico(medico)
                .build();

        historiaClinicaService.ejecutarAgregarDetalle(historia.getPaciente().getDni(), nuevoDetalle);
    }

    // Metodo de fachada para dar de baja logica un detalle clinico
    public void eliminarDetalleDeHistoria(String idHistoria, String idDetalle) {
        historiaClinicaService.ejecutarBajaDetalle(idHistoria, idDetalle);
    }

    // Metodos de consulta expuestos por la fachada para que el controlador no necesite
    // acoplarse a cada servicio individual para poblar las vistas
    public List<HistoriaClinica> listarHistoriasActivas() {
        return historiaClinicaService.listarHistoriasActivas();
    }

    public HistoriaClinica obtenerHistoriaPorId(String idHistoria) {
        return historiaClinicaService.obtenerHistoriaPorId(idHistoria);
    }

    public List<Paciente> listarPacientesActivos() {
        return pacienteService.listarActivos();
    }

    public List<Medico> listarMedicosActivos() {
        return medicoService.listarActivos();
    }

    // Metodo de fachada que coordina la busqueda de la historia clinica y utiliza el patron Strategy
    // (a traves de ExportadorHistoriaContext) para exportarla al formato solicitado (txt).
    public ExportarHistoriaStrategy prepararEstrategiaExportacion(String formato) {
        return exportadorHistoriaContext.seleccionarEstrategia(formato);
    }

    public byte[] exportarHistoriaClinica(String idHistoria, String formato) {
        HistoriaClinica historia = historiaClinicaService.obtenerHistoriaPorId(idHistoria);
        exportadorHistoriaContext.seleccionarEstrategia(formato);
        return exportadorHistoriaContext.ejecutarExportacion(historia);
    }
}

