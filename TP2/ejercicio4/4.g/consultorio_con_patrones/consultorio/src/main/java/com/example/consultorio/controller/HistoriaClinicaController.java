package com.example.consultorio.controller;

import com.example.consultorio.entities.HistoriaClinica;
import com.example.consultorio.facade.ConsultorioFacade;
import com.example.consultorio.strategy.ExportarHistoriaStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Gracias al patron Facade (ConsultorioFacade), este controlador web queda completamente
// desacoplado de la complejidad del subsistema (PacienteService, MedicoService, UsuarioService,
// HistoriaClinicaService y los Builders de las entidades). Solo se comunica con la fachada
// pasandole los IDs y el texto plano recibidos desde los formularios HTML.
@Controller
@RequestMapping("/historias")
public class HistoriaClinicaController {

    @Autowired
    private ConsultorioFacade consultorioFacade;

    @GetMapping("/list")
    public String listar(Model model) {
        model.addAttribute("historias", consultorioFacade.listarHistoriasActivas());
        return "historias/lista";
    }

    @GetMapping("/registrar")
    public String formularioRegistro(Model model) {
        model.addAttribute("pacientes", consultorioFacade.listarPacientesActivos());
        model.addAttribute("medicos", consultorioFacade.listarMedicosActivos());
        return "historias/registrar";
    }

    @PostMapping("/registrar")
    public String registrar(@RequestParam("dniPaciente") String dniPaciente,
                            @RequestParam("medicoId") String medicoId,
                            @RequestParam("detalleHistoria") String detalleHistoria,
                            RedirectAttributes attributes,
                            Model model) {
        try {
            // Una unica llamada a la fachada pasando los IDs y el texto del formulario
            consultorioFacade.registrarHistoriaClinica(dniPaciente, medicoId, detalleHistoria);
            attributes.addFlashAttribute("msgExito", "Historia clínica registrada exitosamente.");
            return "redirect:/historias/list";
        } catch (Exception e) {
            model.addAttribute("pacientes", consultorioFacade.listarPacientesActivos());
            model.addAttribute("medicos", consultorioFacade.listarMedicosActivos());
            model.addAttribute("msgError", e.getMessage());
            return "historias/registrar";
        }
    }

    @GetMapping("/ver/{id}")
    public String verDetalles(@PathVariable String id, Model model) {
        HistoriaClinica historia = consultorioFacade.obtenerHistoriaPorId(id);
        model.addAttribute("historia", historia);
        model.addAttribute("medicos", consultorioFacade.listarMedicosActivos());
        return "historias/ver";
    }

    @PostMapping("/{id}/agregar-detalle")
    public String agregarDetalle(@PathVariable String id,
                                 @RequestParam("medicoId") String medicoId,
                                 @RequestParam("detalleHistoria") String detalleHistoria,
                                 RedirectAttributes attributes) {
        try {
            // Una unica llamada a la fachada pasando los IDs y el texto del formulario
            consultorioFacade.agregarDetalleAHistoria(id, medicoId, detalleHistoria);
            attributes.addFlashAttribute("msgExito", "Nuevo detalle agregado correctamente.");
        } catch (Exception e) {
            attributes.addFlashAttribute("msgError", e.getMessage());
        }
        return "redirect:/historias/ver/" + id;
    }

    @GetMapping("/{id}/eliminar-detalle/{detalleId}")
    public String eliminarDetalle(@PathVariable String id,
                                  @PathVariable String detalleId,
                                  RedirectAttributes attributes) {
        try {
            consultorioFacade.eliminarDetalleDeHistoria(id, detalleId);
            attributes.addFlashAttribute("msgExito", "Detalle eliminado correctamente.");
        } catch (Exception e) {
            attributes.addFlashAttribute("msgError", e.getMessage());
        }
        return "redirect:/historias/ver/" + id;
    }

    // Endpoint que permite descargar la historia clinica en formato TXT o CSV
    // delegando en la fachada, la cual aplica internamente el patron Strategy.
    @GetMapping("/{id}/exportar")
    public ResponseEntity<byte[]> exportarHistoria(@PathVariable String id,
                                                   @RequestParam(value = "formato", defaultValue = "txt") String formato) {
        HistoriaClinica historia = consultorioFacade.obtenerHistoriaPorId(id);
        ExportarHistoriaStrategy strategy = consultorioFacade.prepararEstrategiaExportacion(formato);
        byte[] contenido = consultorioFacade.exportarHistoriaClinica(id, formato);

        String dni = historia.getPaciente() != null ? historia.getPaciente().getDni() : id;
        String nombreArchivo = "historia_clinica_" + dni + strategy.getExtension();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                .contentType(MediaType.parseMediaType(strategy.getContentType()))
                .body(contenido);
    }
}
