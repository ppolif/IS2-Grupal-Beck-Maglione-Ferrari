package com.example.zero.controllers;

import com.example.zero.entidades.persona.Usuario;
import com.example.zero.enums.RolUsuario;
import com.example.zero.services.persona.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AuthController authController;

    @Test
    void showLoginPage_sinSesion_retornaVistaLogin() {
        when(session.getAttribute("usuariosession")).thenReturn(null);

        String vista = authController.showLoginPage(session);

        assertEquals("admin/page-login", vista);
    }

    @Test
    void showLoginPage_conSesionAdmin_redirigeAdmin() {
        Usuario admin = Usuario.builder().rol(RolUsuario.ADMINISTRATIVO).build();
        when(session.getAttribute("usuariosession")).thenReturn(admin);

        String vista = authController.showLoginPage(session);

        assertEquals("redirect:/admin", vista);
    }

    @Test
    void showLoginPage_conSesionCliente_redirigeInicio() {
        Usuario cliente = Usuario.builder().rol(RolUsuario.CLIENTE).build();
        when(session.getAttribute("usuariosession")).thenReturn(cliente);

        String vista = authController.showLoginPage(session);

        assertEquals("redirect:/", vista);
    }

    @Test
    void processLogin_credencialesCorrectasAdmin_guardaSesionYRedirigeAdmin() {
        Usuario admin = Usuario.builder().nombreUsuario("admin@zero.com").rol(RolUsuario.ADMINISTRATIVO).build();
        when(usuarioService.autenticar("admin@zero.com", "admin123")).thenReturn(admin);

        String vista = authController.processLogin("admin@zero.com", "admin123", session, model);

        assertEquals("redirect:/admin", vista);
        verify(session, times(1)).setAttribute("usuariosession", admin);
    }

    @Test
    void processLogin_credencialesCorrectasCliente_guardaSesionYRedirigeInicio() {
        Usuario cliente = Usuario.builder().nombreUsuario("cli@zero.com").rol(RolUsuario.CLIENTE).build();
        when(usuarioService.autenticar("cli@zero.com", "cli123")).thenReturn(cliente);

        String vista = authController.processLogin("cli@zero.com", "cli123", session, model);

        assertEquals("redirect:/", vista);
        verify(session, times(1)).setAttribute("usuariosession", cliente);
    }

    @Test
    void processLogin_credencialesIncorrectas_retornaVistaConError() {
        when(usuarioService.autenticar("invalido@zero.com", "wrong"))
                .thenThrow(new IllegalArgumentException("Credenciales incorrectas"));

        String vista = authController.processLogin("invalido@zero.com", "wrong", session, model);

        assertEquals("admin/page-login", vista);
        verify(model, times(1)).addAttribute("errorMessage", "Credenciales incorrectas");
        verify(session, never()).setAttribute(eq("usuariosession"), any());
    }

    @Test
    void showRegisterPage_sinSesion_retornaVistaRegister() {
        when(session.getAttribute("usuariosession")).thenReturn(null);

        String vista = authController.showRegisterPage(session);

        assertEquals("admin/page-register", vista);
    }

    @Test
    void processRegister_clavesNoCoinciden_retornaVistaConError() {
        String vista = authController.processRegister("Juan", "juan@test.com", "pass1", "pass2", request, session, model);

        assertEquals("admin/page-register", vista);
        verify(model, times(1)).addAttribute("errorMessage", "Las contraseñas no coinciden");
        verify(usuarioService, never()).crearUsuario(any(), any(), any(), any());
    }

    @Test
    void processRegister_exitoso_creaUsuarioYRedirige() {
        Usuario nuevo = Usuario.builder().nombreUsuario("nuevo@zero.com").rol(RolUsuario.ADMINISTRATIVO).build();
        when(request.getRequestURI()).thenReturn("/admin/register");
        when(usuarioService.crearUsuario(eq("nuevo@zero.com"), eq("pass123"), eq(RolUsuario.ADMINISTRATIVO), isNull()))
                .thenReturn(nuevo);

        String vista = authController.processRegister("Nuevo Admin", "nuevo@zero.com", "pass123", "pass123", request, session, model);

        assertEquals("redirect:/admin", vista);
        verify(session, times(1)).setAttribute("usuariosession", nuevo);
    }

    @Test
    void logout_invalidaSesionYRedirigeConLogoutParam() {
        String vista = authController.logout(session);

        assertEquals("redirect:/admin/login?logout=true", vista);
        verify(session, times(1)).invalidate();
    }
}

