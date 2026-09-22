package com.example.zero.controllers;

import com.example.zero.dto.persona.ClienteRegistroDTO;
import com.example.zero.entidades.persona.Usuario;
import com.example.zero.enums.RolUsuario;
import com.example.zero.repositories.NacionalidadRepository;
import com.example.zero.services.persona.ClienteService;
import com.example.zero.services.persona.UsuarioService;
import com.example.zero.services.zona.ZonaService;
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
    private ClienteService clienteService;

    @Mock
    private ZonaService zonaService;

    @Mock
    private NacionalidadRepository nacionalidadRepository;

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
    void showAdminRegisterPage_sinSesion_retornaVistaAdminRegister() {
        when(session.getAttribute("usuariosession")).thenReturn(null);

        String vista = authController.showAdminRegisterPage(session);

        assertEquals("admin/page-register", vista);
    }

    @Test
    void processAdminRegister_clavesNoCoinciden_retornaVistaConError() {
        String vista = authController.processAdminRegister("Juan", "juan@test.com", "pass1", "pass2", request, session, model);

        assertEquals("admin/page-register", vista);
        verify(model, times(1)).addAttribute("errorMessage", "Las contraseñas no coinciden");
        verify(usuarioService, never()).crearUsuario(any(), any(), any(), any());
    }

    @Test
    void processAdminRegister_exitoso_creaUsuarioYRedirigeAdmin() {
        Usuario nuevo = Usuario.builder().nombreUsuario("nuevo@zero.com").rol(RolUsuario.ADMINISTRATIVO).build();
        when(usuarioService.crearUsuario(eq("nuevo@zero.com"), eq("pass123"), eq(RolUsuario.ADMINISTRATIVO), isNull()))
                .thenReturn(nuevo);

        String vista = authController.processAdminRegister("Nuevo Admin", "nuevo@zero.com", "pass123", "pass123", request, session, model);

        assertEquals("redirect:/admin", vista);
        verify(session, times(1)).setAttribute("usuariosession", nuevo);
    }

    @Test
    void showRegisterPage_sinSesion_retornaVistaShopRegister() {
        when(session.getAttribute("usuariosession")).thenReturn(null);

        String vista = authController.showRegisterPage(session, model);

        assertEquals("shop/register", vista);
        verify(model, times(1)).addAttribute(eq("dto"), any(ClienteRegistroDTO.class));
    }

    @Test
    void processRegister_clienteExitoso_guardaSesionYRedirige() {
        ClienteRegistroDTO dto = ClienteRegistroDTO.builder().email("cliente@zero.com").build();
        Usuario usuarioMock = Usuario.builder().nombreUsuario("cliente@zero.com").rol(RolUsuario.CLIENTE).build();

        when(clienteService.registrarCliente(dto)).thenReturn(usuarioMock);

        String vista = authController.processRegister(dto, session, model);

        assertEquals("redirect:/?registered=true", vista);
        verify(session, times(1)).setAttribute("usuariosession", usuarioMock);
    }

    @Test
    void processRegister_errorValidacion_retornaVistaShopRegisterConError() {
        ClienteRegistroDTO dto = ClienteRegistroDTO.builder().email("cliente@zero.com").build();

        when(clienteService.registrarCliente(dto)).thenThrow(new IllegalArgumentException("El documento ya existe"));

        String vista = authController.processRegister(dto, session, model);

        assertEquals("shop/register", vista);
        verify(model, times(1)).addAttribute("errorMessage", "El documento ya existe");
        verify(session, never()).setAttribute(eq("usuariosession"), any());
    }

    @Test
    void processRegister_accionCambiarPais_recargaProvinciasSinRegistrar() {
        ClienteRegistroDTO dto = ClienteRegistroDTO.builder().paisId("pais-arg").provinciaId("prov-cba").build();

        String vista = authController.processRegister(dto, "cambiarPais", session, model);

        assertEquals("shop/register", vista);
        assertNull(dto.getProvinciaId());
        verify(clienteService, never()).registrarCliente(any());
        verify(zonaService, times(1)).listarProvinciasPorPais("pais-arg");
    }

    @Test
    void processRegister_accionCambiarProvincia_recargaDepartamentosSinRegistrar() {
        ClienteRegistroDTO dto = ClienteRegistroDTO.builder().provinciaId("prov-cba").departamentoId("dep-cap").build();

        String vista = authController.processRegister(dto, "cambiarProvincia", session, model);

        assertEquals("shop/register", vista);
        assertNull(dto.getDepartamentoId());
        verify(clienteService, never()).registrarCliente(any());
        verify(zonaService, times(1)).listarDepartamentosPorProvincia("prov-cba");
    }

    @Test
    void adminLogout_invalidaSesionYRedirigeLogin() {
        String vista = authController.adminLogout(session);

        assertEquals("redirect:/admin/login?logout=true", vista);
        verify(session, times(1)).invalidate();
    }

    @Test
    void clientLogout_invalidaSesionYRedirigeInicio() {
        String vista = authController.clientLogout(session);

        assertEquals("redirect:/?logout=true", vista);
        verify(session, times(1)).invalidate();
    }
}
