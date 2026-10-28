package com.example.consultorio.controller;

import com.example.consultorio.entities.DetalleHistoriaClinica;
import com.example.consultorio.entities.HistoriaClinica;
import com.example.consultorio.service.HistoriaClinicaService;
import com.example.consultorio.service.MedicoService;
import com.example.consultorio.service.PacienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/historias")
public class HistoriaClinicaController {

    @Autowired
    private HistoriaClinicaService historiaClinicaService;

    @Autowired
    private PacienteService pacienteService;

    @Autowired
    private MedicoService medicoService;

    @GetMapping("/list")
    public String listar(Model model) {
        model.addAttribute("historias", historiaClinicaService.listarHistoriasActivas());
        return "historias/lista";
    }

    @GetMapping("/registrar")
    public String formularioRegistro(Model model) {
        model.addAttribute("pacientes", pacienteService.listarActivos());
        model.addAttribute("medicos", medicoService.listarActivos());
        model.addAttribute("detalle", new DetalleHistoriaClinica());
        return "historias/registrar";
    }

    @PostMapping("/registrar")
    public String registrar(@RequestParam("dniPaciente") String dniPaciente,
                            @RequestParam("medicoId") String medicoId,
                            @ModelAttribute("detalle") DetalleHistoriaClinica detalle,
                            RedirectAttributes attributes,
                            Model model) {
        try {
            detalle.setMedico(medicoService.buscarPorId(medicoId));
            historiaClinicaService.ejecutarRegistro(dniPaciente, detalle);
            attributes.addFlashAttribute("msgExito", "Historia clínica registrada exitosamente.");
            return "redirect:/historias/list";
        } catch (Exception e) {
            model.addAttribute("pacientes", pacienteService.listarActivos());
            model.addAttribute("medicos", medicoService.listarActivos());
            model.addAttribute("msgError", e.getMessage());
            return "historias/registrar";
        }
    }

    @GetMapping("/ver/{id}")
    public String verDetalles(@PathVariable String id, Model model) {
        HistoriaClinica historia = historiaClinicaService.obtenerHistoriaPorId(id);
        model.addAttribute("historia", historia);
        model.addAttribute("medicos", medicoService.listarActivos());
        model.addAttribute("nuevoDetalle", new DetalleHistoriaClinica());
        return "historias/ver";
    }

    @PostMapping("/{id}/agregar-detalle")
    public String agregarDetalle(@PathVariable String id,
                                 @RequestParam("medicoId") String medicoId,
                                 @ModelAttribute("nuevoDetalle") DetalleHistoriaClinica nuevoDetalle,
                                 RedirectAttributes attributes) {
        try {
            HistoriaClinica historia = historiaClinicaService.obtenerHistoriaPorId(id);
            nuevoDetalle.setMedico(medicoService.buscarPorId(medicoId));
            historiaClinicaService.ejecutarAgregarDetalle(historia.getPaciente().getDni(), nuevoDetalle);
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
            historiaClinicaService.ejecutarBajaDetalle(id, detalleId);
            attributes.addFlashAttribute("msgExito", "Detalle eliminado correctamente.");
        } catch (Exception e) {
            attributes.addFlashAttribute("msgError", e.getMessage());
        }
        return "redirect:/historias/ver/" + id;
    }
}
