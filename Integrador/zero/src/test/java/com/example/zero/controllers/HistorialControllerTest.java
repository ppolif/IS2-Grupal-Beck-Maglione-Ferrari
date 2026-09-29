package com.example.zero.controllers;

import com.example.zero.entidades.compra.Factura;
import com.example.zero.entidades.persona.Cliente;
import com.example.zero.entidades.persona.Usuario;
import com.example.zero.enums.RolUsuario;
import com.example.zero.services.OrdenCompraService;
import com.example.zero.services.VentaService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HistorialControllerTest {

    @Mock
    private VentaService ventaService;

    @Mock
    private OrdenCompraService ordenCompraService;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private HistorialController historialController;

    @Test
    void verHistorial_sinUsuarioEnSesion_redirigeALogin() {
        when(session.getAttribute("usuariosession")).thenReturn(null);

        String vista = historialController.verHistorial(session, model, redirectAttributes);

        assertEquals("redirect:/login", vista);
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("errorMessage"), anyString());
        verify(ventaService, never()).listarComprasCliente(any());
    }

    @Test
    void verHistorial_conUsuarioAdmin_redirigeAAdminVentas() {
        Usuario admin = Usuario.builder().id("u-admin").rol(RolUsuario.ADMINISTRATIVO).build();
        when(session.getAttribute("usuariosession")).thenReturn(admin);

        String vista = historialController.verHistorial(session, model, redirectAttributes);

        assertEquals("redirect:/admin/ventas", vista);
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("infoMessage"), anyString());
        verify(ventaService, never()).listarComprasCliente(any());
    }

    @Test
    void verHistorial_conUsuarioCliente_cargaComprasYRetornaVista() {
        Usuario usuario = Usuario.builder().id("u-cli").rol(RolUsuario.CLIENTE).nombreUsuario("cliente@test.com").build();
        Cliente cliente = Cliente.builder().numeroDocumento("12345678").nombre("Lionel").apellido("Messi").build();
        Factura f1 = Factura.builder().id("fac-1").numeroFactura(2001L).totalPagado(15000.0).build();

        when(session.getAttribute("usuariosession")).thenReturn(usuario);
        when(ordenCompraService.obtenerOAsociarCliente(usuario)).thenReturn(cliente);
        when(ventaService.listarComprasCliente(cliente)).thenReturn(List.of(f1));

        String vista = historialController.verHistorial(session, model, redirectAttributes);

        assertEquals("shop/historial", vista);
        verify(model, times(1)).addAttribute("compras", List.of(f1));
        verify(model, times(1)).addAttribute("totalCompras", 1);
        verify(model, times(1)).addAttribute("totalGastado", 15000.0);
        verify(model, times(1)).addAttribute("cliente", cliente);
    }

    @Test
    void verDetalleCompra_redirigeAConfirmationConOrderNumber() {
        String vista = historialController.verDetalleCompra("#ORD-2001");
        assertEquals("redirect:/shop/confirmation?orderNumber=#ORD-2001", vista);
    }
}
