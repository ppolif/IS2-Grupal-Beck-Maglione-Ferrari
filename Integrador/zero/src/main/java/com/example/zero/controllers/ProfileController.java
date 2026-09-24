package com.example.zero.controllers;

import com.example.zero.entidades.persona.Cliente;
import com.example.zero.entidades.persona.Usuario;
import com.example.zero.enums.RolUsuario;
import com.example.zero.repositories.ClienteRepository;
import com.example.zero.repositories.UsuarioRepository;
import com.example.zero.services.persona.UsuarioService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;

    @GetMapping("/profile")
    public String verPerfil(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Usuario usuarioSession = (Usuario) session.getAttribute("usuariosession");
        if (usuarioSession == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Debes iniciar sesión para acceder a tu perfil.");
            return "redirect:/login";
        }

        Usuario usuarioActualizado = usuarioRepository.findById(usuarioSession.getId()).orElse(usuarioSession);
        model.addAttribute("usuario", usuarioActualizado);

        if (usuarioActualizado.getPersona() instanceof Cliente cliente) {
            model.addAttribute("cliente", cliente);
        } else if (usuarioActualizado.getPersona() != null && usuarioActualizado.getPersona().getNumeroDocumento() != null) {
            clienteRepository.findByNumeroDocumentoAndEliminadoFalse(usuarioActualizado.getPersona().getNumeroDocumento())
                    .ifPresent(c -> model.addAttribute("cliente", c));
        }

        return "shop/profile";
    }

    @PostMapping("/profile")
    public String actualizarPerfil(HttpSession session,
                                   @RequestParam("email") String email,
                                   @RequestParam(value = "foto", required = false) String foto,
                                   RedirectAttributes redirectAttributes) {
        Usuario usuarioSession = (Usuario) session.getAttribute("usuariosession");
        if (usuarioSession == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Sesión expirada. Inicia sesión nuevamente.");
            return "redirect:/login";
        }

        try {
            Usuario actualizado = usuarioService.actualizarPerfil(usuarioSession.getId(), email, foto);
            session.setAttribute("usuariosession", actualizado);
            redirectAttributes.addFlashAttribute("successMessage", "Perfil actualizado con éxito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar perfil: " + e.getMessage());
        }

        return "redirect:/profile";
    }
}

