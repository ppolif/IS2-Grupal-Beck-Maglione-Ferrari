package com.example.zero.controllers;

import com.example.zero.entidades.persona.Empleado;
import com.example.zero.entidades.persona.Usuario;
import com.example.zero.enums.RolUsuario;
import com.example.zero.repositories.EmpleadoRepository;
import com.example.zero.repositories.UsuarioRepository;
import com.example.zero.services.persona.UsuarioService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/profile")
@RequiredArgsConstructor
public class AdminProfileController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final EmpleadoRepository empleadoRepository;

    @GetMapping
    public String verPerfilAdmin(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Usuario usuarioSession = (Usuario) session.getAttribute("usuariosession");
        if (usuarioSession == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Debes iniciar sesión para acceder al panel de administración.");
            return "redirect:/login";
        }

        if (usuarioSession.getRol() == RolUsuario.CLIENTE) {
            return "redirect:/profile";
        }

        Usuario usuarioActualizado = usuarioRepository.findById(usuarioSession.getId()).orElse(usuarioSession);
        model.addAttribute("usuario", usuarioActualizado);

        if (usuarioActualizado.getPersona() instanceof Empleado empleado) {
            model.addAttribute("empleado", empleado);
        } else if (usuarioActualizado.getPersona() != null && usuarioActualizado.getPersona().getNumeroDocumento() != null) {
            empleadoRepository.findByNumeroDocumentoAndEliminadoFalse(usuarioActualizado.getPersona().getNumeroDocumento())
                    .ifPresent(e -> model.addAttribute("empleado", e));
        }

        return "admin/profile";
    }

    @PostMapping
    public String actualizarPerfilAdmin(HttpSession session,
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
            redirectAttributes.addFlashAttribute("successMessage", "Perfil de administrador actualizado con éxito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar perfil: " + e.getMessage());
        }

        return "redirect:/admin/profile";
    }
}

