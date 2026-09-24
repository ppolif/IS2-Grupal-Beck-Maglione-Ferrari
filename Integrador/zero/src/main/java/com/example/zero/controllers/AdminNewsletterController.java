package com.example.zero.controllers;

import com.example.zero.services.mail.NewsletterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/newsletter")
@RequiredArgsConstructor
public class AdminNewsletterController {

    private final NewsletterService newsletterService;

    @PostMapping("/enviar")
    public String dispararNewsletterManual(RedirectAttributes redirectAttributes) {
        try {
            int enviados = newsletterService.enviarNewsletterOfertas();
            if (enviados > 0) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Newsletter de ofertas enviado con éxito a " + enviados + " usuarios registrados.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "No se enviaron correos: no hay productos en oferta activos o no hay usuarios registrados.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error al despachar el newsletter: " + e.getMessage());
        }
        return "redirect:/admin";
    }
}
