package com.example.consultorio.controller;

import com.example.consultorio.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/")
    public String inicio() {
        return "redirect:/pacientes/list";
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        if (error != null) {
            model.addAttribute("msgError", "Usuario o contraseña incorrectos.");
        }
        if (logout != null) {
            model.addAttribute("msgExito", "Ha cerrado sesión correctamente.");
        }
        return "auth/login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/login?logout=true";
    }

    @GetMapping("/registro")
    public String registro() {
        return "auth/register";
    }

    @PostMapping("/registro")
    public String procesarRegistro(@RequestParam("nombreUsuario") String nombreUsuario,
                                   @RequestParam("clave") String clave,
                                   Model model) {
        try {
            usuarioService.registrarUsuario(nombreUsuario, clave);
            model.addAttribute("msgExito", "Usuario registrado con éxito. Ya puede iniciar sesión.");
            return "auth/login";
        } catch (Exception e) {
            model.addAttribute("msgError", e.getMessage());
            return "auth/register";
        }
    }
}
