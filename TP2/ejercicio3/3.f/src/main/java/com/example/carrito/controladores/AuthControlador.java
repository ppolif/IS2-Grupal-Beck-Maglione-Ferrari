package com.example.carrito.controladores;

import com.example.carrito.dtos.UsuarioRegistroDTO;
import com.example.carrito.excepciones.ErrorServicio;
import com.example.carrito.servicios.UsuarioServicio;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthControlador {

    @Autowired
    private UsuarioServicio usuarioServicio;

    @GetMapping("/login")
    public String login(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            Model model
    ) {
        if (error != null) {
            model.addAttribute("error", "Email o contraseña incorrectos. Por favor verifique sus datos.");
        }
        if (logout != null) {
            model.addAttribute("exito", "Ha cerrado sesión correctamente.");
        }
        return "auth/login";
    }

    @GetMapping("/registro")
    public String registro(Model model) {
        model.addAttribute("usuario", new UsuarioRegistroDTO());
        return "auth/registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(
            @Valid @ModelAttribute("usuario") UsuarioRegistroDTO dto,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/registro";
        }

        try {
            usuarioServicio.registrar(dto);
            return "redirect:/login?registrado=true";
        } catch (ErrorServicio e) {
            model.addAttribute("error", e.getMessage());
            return "auth/registro";
        }
    }
}

