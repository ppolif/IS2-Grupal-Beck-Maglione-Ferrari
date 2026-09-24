package com.example.zero.controllers;

import com.example.zero.entidades.compra.Factura;
import com.example.zero.entidades.compraCliente.DetalleCompra;
import com.example.zero.entidades.compraCliente.OrdenCompra;
import com.example.zero.entidades.persona.Cliente;
import com.example.zero.entidades.persona.Usuario;
import com.example.zero.enums.RolUsuario;
import com.example.zero.services.MercadoPagoService;
import com.example.zero.services.OrdenCompraService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagosControllerTest {

    @Mock
    private MercadoPagoService mercadoPagoService;

    @Mock
    private OrdenCompraService ordenCompraService;

    @Mock
    private HttpSession session;

    @Mock
    private HttpServletRequest request;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private PagosController pagosController;

    private Usuario usuarioCliente;
    private Cliente clienteMock;
    private OrdenCompra carritoMock;

    @BeforeEach
    void setUp() {
        usuarioCliente = Usuario.builder()
                .id("usr-1")
                .nombreUsuario("cliente@test.com")
                .rol(RolUsuario.CLIENTE)
                .build();

        clienteMock = Cliente.builder()
                .numeroDocumento("35123456")
                .nombre("Juan")
                .apellido("Perez")
                .build();

        carritoMock = OrdenCompra.builder()
                .id("cart-123")
                .cliente(clienteMock)
                .detalles(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("mercadoPago redirige a login si el usuario no está en sesión")
    void mercadoPago_sinUsuarioEnSesion_redirigeALogin() {
        when(session.getAttribute("usuariosession")).thenReturn(null);

        String vista = pagosController.mercadoPago(session, request, redirectAttributes);

        assertEquals("redirect:/login", vista);
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), anyString());
    }

    @Test
    @DisplayName("mercadoPago redirige a admin si el usuario no es CLIENTE")
    void mercadoPago_usuarioAdmin_redirigeAAdmin() {
        Usuario admin = Usuario.builder().rol(RolUsuario.ADMINISTRATIVO).build();
        when(session.getAttribute("usuariosession")).thenReturn(admin);

        String vista = pagosController.mercadoPago(session, request, redirectAttributes);

        assertEquals("redirect:/admin", vista);
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), anyString());
    }

    @Test
    @DisplayName("mercadoPago redirige al carrito si está vacío")
    void mercadoPago_carritoVacio_redirigeACarrito() {
        when(session.getAttribute("usuariosession")).thenReturn(usuarioCliente);
        when(ordenCompraService.obtenerOAsociarCliente(usuarioCliente)).thenReturn(clienteMock);
        when(ordenCompraService.obtenerOCrearCarrito(clienteMock)).thenReturn(carritoMock);
        when(ordenCompraService.obtenerItemsActivos(carritoMock)).thenReturn(List.of());

        String vista = pagosController.mercadoPago(session, request, redirectAttributes);

        assertEquals("redirect:/shop/cart", vista);
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), anyString());
    }

    @Test
    @DisplayName("mercadoPago redirige a init_point de Mercado Pago con éxito")
    void mercadoPago_conItems_redirigeAInitPoint() throws Exception {
        when(session.getAttribute("usuariosession")).thenReturn(usuarioCliente);
        when(ordenCompraService.obtenerOAsociarCliente(usuarioCliente)).thenReturn(clienteMock);
        when(ordenCompraService.obtenerOCrearCarrito(clienteMock)).thenReturn(carritoMock);

        DetalleCompra item = DetalleCompra.builder().cantidad(2).precioUnitario(500.0).build();
        when(ordenCompraService.obtenerItemsActivos(carritoMock)).thenReturn(List.of(item));

        when(request.getHeader("X-Forwarded-Proto")).thenReturn(null);
        when(request.getHeader("X-Forwarded-Host")).thenReturn(null);
        when(request.getScheme()).thenReturn("http");
        when(request.getServerName()).thenReturn("localhost");
        when(request.getServerPort()).thenReturn(8080);
        when(request.getContextPath()).thenReturn("");

        String urlEsperada = "https://www.mercadopago.com.ar/checkout/v1/redirect?pref_id=123456";
        when(mercadoPagoService.crearPreferenciaParaCarrito(eq(carritoMock), eq(clienteMock), anyString()))
                .thenReturn(urlEsperada);

        String vista = pagosController.mercadoPago(session, request, redirectAttributes);

        assertEquals("redirect:" + urlEsperada, vista);
    }

    @Test
    @DisplayName("pagoExitoso procesa orden y redirige a confirmation con orderNumber")
    void pagoExitoso_procesaPagoYRedirigeAConfirmacion() {
        Factura facturaMock = Factura.builder().id("fac-1").numeroFactura(1050L).build();
        when(mercadoPagoService.procesarPagoExitoso(eq("cart-123"), eq("pay-99"), any()))
                .thenReturn(facturaMock);

        String vista = pagosController.pagoExitoso("pay-99", "approved", "cart-123", "approved", session, redirectAttributes);

        assertEquals("redirect:/shop/confirmation?orderNumber=1050", vista);
        verify(redirectAttributes).addFlashAttribute(eq("successMessage"), anyString());
    }

    @Test
    @DisplayName("obtenerUrlMercadoPago retorna JSON con URL de checkout")
    void obtenerUrlMercadoPago_usuarioValido_retornaMapaConUrl() throws Exception {
        when(session.getAttribute("usuariosession")).thenReturn(usuarioCliente);
        when(ordenCompraService.obtenerOAsociarCliente(usuarioCliente)).thenReturn(clienteMock);
        when(ordenCompraService.obtenerOCrearCarrito(clienteMock)).thenReturn(carritoMock);

        when(request.getHeader("X-Forwarded-Proto")).thenReturn(null);
        when(request.getHeader("X-Forwarded-Host")).thenReturn(null);
        when(request.getScheme()).thenReturn("http");
        when(request.getServerName()).thenReturn("localhost");
        when(request.getServerPort()).thenReturn(8080);
        when(request.getContextPath()).thenReturn("");

        when(mercadoPagoService.crearPreferenciaParaCarrito(any(), any(), anyString()))
                .thenReturn("https://mercadopago.com/init");

        Map<String, String> res = pagosController.obtenerUrlMercadoPago(session, request);

        assertNotNull(res);
        assertEquals("https://mercadopago.com/init", res.get("url"));
    }
}
