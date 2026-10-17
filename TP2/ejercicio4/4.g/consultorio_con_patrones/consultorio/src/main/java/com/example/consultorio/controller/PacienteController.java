package com.example.consultorio.controller;

import com.example.consultorio.entities.Paciente;
import com.example.consultorio.service.FotoService;
import com.example.consultorio.service.PacienteService;
import com.example.consultorio.template.BaseUseCaseController;
import com.example.consultorio.template.BaseWebController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pacientes")
public class PacienteController extends BaseWebController<Paciente> {

    @Autowired
    private FotoService fotoService;

    public PacienteController(PacienteService service) {
        super(service, "pacientes", "Listado de Pacientes", "Paciente");
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("item") Paciente paciente,
                          @RequestParam(value = "archivoFoto", required = false) MultipartFile archivoFoto,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        try {
            if (paciente.getId() == null || paciente.getId().trim().isEmpty()) {
                paciente.setId(null);
                service.ejecutarAlta(paciente);
            } else {
                service.ejecutarModificacion(paciente);
            }

            // Manejo de la foto (HDU Subir foto paciente)
            if (archivoFoto != null && !archivoFoto.isEmpty()) {
                fotoService.asignarNuevaFoto(paciente, archivoFoto);
            }

            redirectAttributes.addFlashAttribute("msgExito", "Paciente guardado con éxito.");
            return redirectList;
        } catch (Exception e) {
            model.addAttribute("item", paciente);
            model.addAttribute("titleEdit", titleEdit);
            model.addAttribute("isDisabled", false);
            model.addAttribute("msgError", e.getMessage());
            return viewEdit;
        }
    }
}
