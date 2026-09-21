package com.example.zero.controllers;

import com.example.zero.entidades.persona.Usuario;
import com.example.zero.enums.RolUsuario;
import com.example.zero.services.persona.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping({"/admin/login", "/login"})
    public String showLoginPage(HttpSession session) {
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuariosession");
        if (usuarioLogueado != null) {
            if (usuarioLogueado.getRol() == RolUsuario.ADMINISTRATIVO || usuarioLogueado.getRol() == RolUsuario.JEFE) {
                return "redirect:/admin";
            }
            return "redirect:/";
        }
        return "admin/page-login";
    }

    @PostMapping({"/admin/login", "/login"})
    public String processLogin(@RequestParam("username") String username,
                               @RequestParam("password") String password,
                               HttpSession session,
                               Model model) {
        try {
            Usuario usuario = usuarioService.autenticar(username, password);
            session.setAttribute("usuariosession", usuario);

            if (usuario.getRol() == RolUsuario.ADMINISTRATIVO || usuario.getRol() == RolUsuario.JEFE) {
                return "redirect:/admin";
            }
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/page-login";
        }
    }

    @GetMapping({"/admin/register", "/register"})
    public String showRegisterPage(HttpSession session) {
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuariosession");
        if (usuarioLogueado != null) {
            return "redirect:/admin";
        }
        return "admin/page-register";
    }

    @PostMapping({"/admin/register", "/register"})
    public String processRegister(@RequestParam("nombre") String nombre,
                                  @RequestParam("email") String email,
                                  @RequestParam("password") String password,
                                  @RequestParam("confirmPassword") String confirmPassword,
                                  HttpServletRequest request,
                                  HttpSession session,
                                  Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Las contraseñas no coinciden");
            return "admin/page-register";
        }

        try {
            RolUsuario rol = request.getRequestURI().contains("/admin") 
                    ? RolUsuario.ADMINISTRATIVO 
                    : RolUsuario.CLIENTE;

            Usuario nuevoUsuario = usuarioService.crearUsuario(email, password, rol, null);
            session.setAttribute("usuariosession", nuevoUsuario);

            if (rol == RolUsuario.ADMINISTRATIVO || rol == RolUsuario.JEFE) {
                return "redirect:/admin";
            }
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/page-register";
        }
    }

    @GetMapping("/admin/logout")
    public String adminLogout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/admin/login?logout=true";
    }

    @GetMapping("/logout")
    public String clientLogout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/?logout=true";
    }
}

