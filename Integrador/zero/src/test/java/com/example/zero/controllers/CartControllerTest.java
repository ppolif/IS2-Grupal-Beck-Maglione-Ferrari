package com.example.zero.controllers;

import com.example.zero.entidades.compraCliente.DetalleCompra;
import com.example.zero.entidades.compraCliente.OrdenCompra;
import com.example.zero.entidades.persona.Cliente;
import com.example.zero.entidades.persona.Usuario;
import com.example.zero.enums.RolUsuario;
import com.example.zero.services.OrdenCompraService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private OrdenCompraService ordenCompraService;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private CartController cartController;

    private Usuario usuarioSession;
    private Cliente clienteMock;
    private OrdenCompra carritoMock;

    @BeforeEach
    void setUp() {
        usuarioSession = Usuario.builder()
                .id("user-123")
                .nombreUsuario("cliente@zero.com")
                .rol(RolUsuario.CLIENTE)
                .build();

        clienteMock = Cliente.builder()
                .id("cli-123")
                .numeroDocumento("12345678")
                .nombre("Juan")
                .apellido("Perez")
                .build();

        carritoMock = OrdenCompra.builder()
                .id("cart-123")
                .cliente(clienteMock)
                .detalles(new ArrayList<>())
                .total(0.0)
                .build();
    }

    @Test
    @DisplayName("showCart sin usuario en sesión redirige a /login")
    void showCart_sinSesion_redirigeALogin() {
        when(session.getAttribute("usuariosession")).thenReturn(null);

        String view = cartController.showCart(session, model, redirectAttributes);

        assertEquals("redirect:/login", view);
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), any());
    }

    @Test
    @DisplayName("showCart con usuario administrador redirige a /admin con flash error")
    void showCart_conUsuarioAdmin_redirigeAAdmin() {
        Usuario admin = Usuario.builder()
                .id("admin-1")
                .nombreUsuario("admin@zero.com")
                .rol(RolUsuario.ADMINISTRATIVO)
                .build();
        when(session.getAttribute("usuariosession")).thenReturn(admin);

        String view = cartController.showCart(session, model, redirectAttributes);

        assertEquals("redirect:/admin", view);
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), contains("administradores"));
        verifyNoInteractions(ordenCompraService);
    }

    @Test
    @DisplayName("showCart con usuario en sesión carga datos y retorna vista shop/cart")
    void showCart_conSesion_retornaVistaShopCart() {
        List<DetalleCompra> items = new ArrayList<>();
        when(session.getAttribute("usuariosession")).thenReturn(usuarioSession);
        when(ordenCompraService.obtenerOAsociarCliente(usuarioSession)).thenReturn(clienteMock);
        when(ordenCompraService.obtenerOCrearCarrito(clienteMock)).thenReturn(carritoMock);
        when(ordenCompraService.obtenerItemsActivos(carritoMock)).thenReturn(items);
        when(ordenCompraService.contarItems(carritoMock)).thenReturn(0);

        String view = cartController.showCart(session, model, redirectAttributes);

        assertEquals("shop/cart", view);
        verify(model).addAttribute("cart", carritoMock);
        verify(model).addAttribute("items", items);
        verify(model).addAttribute("totalItems", 0);
    }

    @Test
    @DisplayName("addToCart sin sesión redirige a /login con flash error")
    void addToCart_sinSesion_redirigeALogin() {
        when(session.getAttribute("usuariosession")).thenReturn(null);

        String view = cartController.addToCart("prod-1", 1, null, session, redirectAttributes);

        assertEquals("redirect:/login", view);
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), any());
        verify(ordenCompraService, never()).agregarProducto(any(), any(), anyInt());
    }

    @Test
    @DisplayName("addToCart con sesión y referer redirige al referer con flash success")
    void addToCart_conSesionYReferer_redirigeAlReferer() {
        String referer = "/shop/category?categoryId=cat-1";
        when(session.getAttribute("usuariosession")).thenReturn(usuarioSession);
        when(ordenCompraService.obtenerOAsociarCliente(usuarioSession)).thenReturn(clienteMock);

        String view = cartController.addToCart("prod-1", 2, referer, session, redirectAttributes);

        assertEquals("redirect:" + referer, view);
        verify(ordenCompraService).agregarProducto(clienteMock, "prod-1", 2);
        verify(redirectAttributes).addFlashAttribute(eq("successMessage"), any());
    }

    @Test
    @DisplayName("addToCart sin referer redirige a /shop/category por defecto")
    void addToCart_conSesionSinReferer_redirigeACategory() {
        when(session.getAttribute("usuariosession")).thenReturn(usuarioSession);
        when(ordenCompraService.obtenerOAsociarCliente(usuarioSession)).thenReturn(clienteMock);

        String view = cartController.addToCart("prod-1", 1, null, session, redirectAttributes);

        assertEquals("redirect:/shop/category", view);
        verify(ordenCompraService).agregarProducto(clienteMock, "prod-1", 1);
        verify(redirectAttributes).addFlashAttribute(eq("successMessage"), any());
    }

    @Test
    @DisplayName("updateCartItem actualiza cantidad y redirige a /shop/cart")
    void updateCartItem_actualizaYRedirigeACart() {
        when(session.getAttribute("usuariosession")).thenReturn(usuarioSession);
        when(ordenCompraService.obtenerOAsociarCliente(usuarioSession)).thenReturn(clienteMock);

        String view = cartController.updateCartItem("item-1", 3, session, redirectAttributes);

        assertEquals("redirect:/shop/cart", view);
        verify(ordenCompraService).actualizarCantidad(clienteMock, "item-1", 3);
        verify(redirectAttributes).addFlashAttribute(eq("successMessage"), any());
    }

    @Test
    @DisplayName("removeCartItem elimina ítem y redirige a /shop/cart")
    void removeCartItem_eliminaYRedirigeACart() {
        when(session.getAttribute("usuariosession")).thenReturn(usuarioSession);
        when(ordenCompraService.obtenerOAsociarCliente(usuarioSession)).thenReturn(clienteMock);

        String view = cartController.removeCartItem("item-1", session, redirectAttributes);

        assertEquals("redirect:/shop/cart", view);
        verify(ordenCompraService).eliminarProducto(clienteMock, "item-1");
        verify(redirectAttributes).addFlashAttribute(eq("successMessage"), any());
    }

    @Test
    @DisplayName("clearCart vacía carrito y redirige a /shop/cart")
    void clearCart_vaciaYRedirigeACart() {
        when(session.getAttribute("usuariosession")).thenReturn(usuarioSession);
        when(ordenCompraService.obtenerOAsociarCliente(usuarioSession)).thenReturn(clienteMock);

        String view = cartController.clearCart(session, redirectAttributes);

        assertEquals("redirect:/shop/cart", view);
        verify(ordenCompraService).vaciarCarrito(clienteMock);
        verify(redirectAttributes).addFlashAttribute(eq("successMessage"), any());
    }

    @Test
    @DisplayName("addToCart con usuario administrador redirige a /admin y no agrega producto")
    void addToCart_conAdmin_redirigeAAdmin() {
        Usuario admin = Usuario.builder()
                .id("admin-1")
                .nombreUsuario("admin@zero.com")
                .rol(RolUsuario.ADMINISTRATIVO)
                .build();
        when(session.getAttribute("usuariosession")).thenReturn(admin);

        String view = cartController.addToCart("prod-1", 1, null, session, redirectAttributes);

        assertEquals("redirect:/admin", view);
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), contains("administradores"));
        verify(ordenCompraService, never()).agregarProducto(any(), any(), anyInt());
    }

    @Test
    @DisplayName("updateCartItem con usuario administrador redirige a /admin")
    void updateCartItem_conAdmin_redirigeAAdmin() {
        Usuario jefe = Usuario.builder()
                .id("jefe-1")
                .nombreUsuario("jefe@zero.com")
                .rol(RolUsuario.JEFE)
                .build();
        when(session.getAttribute("usuariosession")).thenReturn(jefe);

        String view = cartController.updateCartItem("item-1", 2, session, redirectAttributes);

        assertEquals("redirect:/admin", view);
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), contains("administradores"));
        verify(ordenCompraService, never()).actualizarCantidad(any(), any(), anyInt());
    }

    @Test
    @DisplayName("removeCartItem con usuario administrador redirige a /admin")
    void removeCartItem_conAdmin_redirigeAAdmin() {
        Usuario admin = Usuario.builder()
                .id("admin-1")
                .nombreUsuario("admin@zero.com")
                .rol(RolUsuario.ADMINISTRATIVO)
                .build();
        when(session.getAttribute("usuariosession")).thenReturn(admin);

        String view = cartController.removeCartItem("item-1", session, redirectAttributes);

        assertEquals("redirect:/admin", view);
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), contains("administradores"));
        verify(ordenCompraService, never()).eliminarProducto(any(), any());
    }

    @Test
    @DisplayName("clearCart con usuario administrador redirige a /admin")
    void clearCart_conAdmin_redirigeAAdmin() {
        Usuario admin = Usuario.builder()
                .id("admin-1")
                .nombreUsuario("admin@zero.com")
                .rol(RolUsuario.ADMINISTRATIVO)
                .build();
        when(session.getAttribute("usuariosession")).thenReturn(admin);

        String view = cartController.clearCart(session, redirectAttributes);

        assertEquals("redirect:/admin", view);
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), contains("administradores"));
        verify(ordenCompraService, never()).vaciarCarrito(any());
    }

    @Test
    @DisplayName("applyCoupon con usuario administrador redirige a /admin")
    void applyCoupon_conAdmin_redirigeAAdmin() {
        Usuario admin = Usuario.builder()
                .id("admin-1")
                .nombreUsuario("admin@zero.com")
                .rol(RolUsuario.ADMINISTRATIVO)
                .build();
        when(session.getAttribute("usuariosession")).thenReturn(admin);

        String view = cartController.applyCoupon("DESCUENTO10", session, redirectAttributes);

        assertEquals("redirect:/admin", view);
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), contains("administradores"));
    }
}

