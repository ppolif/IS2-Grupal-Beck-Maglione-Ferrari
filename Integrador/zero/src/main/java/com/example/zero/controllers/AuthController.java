package com.example.zero.controllers;

import com.example.zero.dto.persona.ClienteRegistroDTO;
import com.example.zero.entidades.persona.Usuario;
import com.example.zero.enums.RolUsuario;
import com.example.zero.enums.TipoDocumento;
import com.example.zero.repositories.NacionalidadRepository;
import com.example.zero.services.persona.ClienteService;
import com.example.zero.services.persona.UsuarioService;
import com.example.zero.services.zona.ZonaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class AuthController {

    private final UsuarioService usuarioService;
    private final ClienteService clienteService;
    private final ZonaService zonaService;
    private final NacionalidadRepository nacionalidadRepository;

    public AuthController(UsuarioService usuarioService) {
        this(usuarioService, null, null, null);
    }

    @Autowired
    public AuthController(UsuarioService usuarioService,
                          ClienteService clienteService,
                          ZonaService zonaService,
                          NacionalidadRepository nacionalidadRepository) {
        this.usuarioService = usuarioService;
        this.clienteService = clienteService;
        this.zonaService = zonaService;
        this.nacionalidadRepository = nacionalidadRepository;
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

    // Registro administrativo básico preexistente
    @GetMapping("/admin/register")
    public String showAdminRegisterPage(HttpSession session) {
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuariosession");
        if (usuarioLogueado != null) {
            return "redirect:/admin";
        }
        return "admin/page-register";
    }

    @PostMapping("/admin/register")
    public String processAdminRegister(@RequestParam("nombre") String nombre,
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
            Usuario nuevoUsuario = usuarioService.crearUsuario(email, password, RolUsuario.ADMINISTRATIVO, null);
            session.setAttribute("usuariosession", nuevoUsuario);
            return "redirect:/admin";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/page-register";
        }
    }

    // Registro público completo para clientes (gestión dinámica 100% Thymeleaf + Java)
    @GetMapping("/register")
    public String showRegisterPage(HttpSession session, Model model) {
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuariosession");
        if (usuarioLogueado != null) {
            return "redirect:/";
        }

        ClienteRegistroDTO dto = ClienteRegistroDTO.builder()
                .tipoDocumento(TipoDocumento.DNI)
                .tipoContacto("EMAIL")
                .build();
        model.addAttribute("dto", dto);
        cargarDatosFormularioRegistro(model, dto);

        return "shop/register";
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute("dto") ClienteRegistroDTO dto,
                                  @RequestParam(value = "accion", required = false) String accion,
                                  HttpSession session,
                                  Model model) {
        // Acciones dinámicas de recarga controladas por Thymeleaf y Spring MVC
        if ("cambiarPais".equals(accion)) {
            dto.setProvinciaId(null);
            dto.setDepartamentoId(null);
            dto.setLocalidadId(null);
            cargarDatosFormularioRegistro(model, dto);
            model.addAttribute("dto", dto);
            return "shop/register";
        }

        if ("cambiarProvincia".equals(accion)) {
            dto.setDepartamentoId(null);
            dto.setLocalidadId(null);
            cargarDatosFormularioRegistro(model, dto);
            model.addAttribute("dto", dto);
            return "shop/register";
        }

        if ("cambiarDepartamento".equals(accion)) {
            dto.setLocalidadId(null);
            cargarDatosFormularioRegistro(model, dto);
            model.addAttribute("dto", dto);
            return "shop/register";
        }

        if ("cambiarContacto".equals(accion) || "actualizarZonas".equals(accion)) {
            cargarDatosFormularioRegistro(model, dto);
            model.addAttribute("dto", dto);
            return "shop/register";
        }

        // Procesamiento del registro de cliente
        try {
            if (clienteService != null) {
                clienteService.registrarCliente(dto);
            }
            String emailDestino = (dto.getEmail() != null) ? dto.getEmail().trim() : "";
            String emailParam = java.net.URLEncoder.encode(emailDestino, java.nio.charset.StandardCharsets.UTF_8);
            return "redirect:/verify?email=" + emailParam + "&sent=true";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("dto", dto);
            cargarDatosFormularioRegistro(model, dto);
            return "shop/register";
        }
    }

    public String processRegister(ClienteRegistroDTO dto,
                                  HttpSession session,
                                  Model model) {
        return processRegister(dto, null, session, model);
    }

    // Endpoints de confirmación y activación de cuenta por correo (rules/CONTROLLERS.md)
    @GetMapping("/verify")
    public String showVerifyPage(@RequestParam(value = "email", required = false) String email,
                                 HttpSession session,
                                 Model model) {
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuariosession");
        if (usuarioLogueado != null) {
            return "redirect:/";
        }

        model.addAttribute("email", email != null ? email.trim() : "");
        return "shop/verify";
    }

    @PostMapping("/verify")
    public String processVerify(@RequestParam("email") String email,
                                @RequestParam("codigo") String codigo,
                                Model model) {
        try {
            if (usuarioService != null) {
                usuarioService.verificarCodigo(email, codigo);
            }
            return "redirect:/login?verified=true";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("email", email != null ? email.trim() : "");
            return "shop/verify";
        }
    }

    @PostMapping("/verify/resend")
    public String resendVerificationCode(@RequestParam("email") String email,
                                         Model model) {
        try {
            if (usuarioService != null) {
                usuarioService.reenviarCodigoConfirmacion(email);
            }
            String emailParam = (email != null) ? java.net.URLEncoder.encode(email.trim(), java.nio.charset.StandardCharsets.UTF_8) : "";
            return "redirect:/verify?email=" + emailParam + "&resent=true";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("email", email != null ? email.trim() : "");
            return "shop/verify";
        }
    }

    private void cargarDatosFormularioRegistro(Model model, ClienteRegistroDTO dto) {
        model.addAttribute("tiposDocumento", TipoDocumento.values());
        if (nacionalidadRepository != null) {
            model.addAttribute("nacionalidades", nacionalidadRepository.findByEliminadoFalse());
        }
        if (zonaService != null) {
            model.addAttribute("paises", zonaService.listarPaisesActivos());

            if (dto != null && dto.getPaisId() != null && !dto.getPaisId().trim().isEmpty()) {
                model.addAttribute("provincias", zonaService.listarProvinciasPorPais(dto.getPaisId()));
            } else {
                model.addAttribute("provincias", List.of());
            }

            if (dto != null && dto.getProvinciaId() != null && !dto.getProvinciaId().trim().isEmpty()) {
                model.addAttribute("departamentos", zonaService.listarDepartamentosPorProvincia(dto.getProvinciaId()));
            } else {
                model.addAttribute("departamentos", List.of());
            }

            if (dto != null && dto.getDepartamentoId() != null && !dto.getDepartamentoId().trim().isEmpty()) {
                model.addAttribute("localidades", zonaService.listarLocalidadesPorDepartamento(dto.getDepartamentoId()));
            } else {
                model.addAttribute("localidades", List.of());
            }
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
