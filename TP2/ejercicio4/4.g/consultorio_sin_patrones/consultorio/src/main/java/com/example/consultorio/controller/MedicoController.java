package com.example.consultorio.controller;

import com.example.consultorio.entities.Medico;
import com.example.consultorio.service.MedicoService;
import com.example.consultorio.template.BaseWebController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/medicos")
public class MedicoController extends BaseWebController<Medico> {

    public MedicoController(MedicoService service) {
        super(service, "medicos", "Listado de Médicos", "Médico");
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("item") Medico medico,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        try {
            if (medico.getId() == null || medico.getId().trim().isEmpty()) {
                medico.setId(null);
                service.ejecutarAlta(medico);
            } else {
                service.ejecutarModificacion(medico);
            }
            redirectAttributes.addFlashAttribute("msgExito", "Médico guardado con éxito.");
            return redirectList;
        } catch (Exception e) {
            model.addAttribute("item", medico);
            model.addAttribute("titleEdit", titleEdit);
            model.addAttribute("isDisabled", false);
            model.addAttribute("msgError", e.getMessage());
            return viewEdit;
        }
    }
}
