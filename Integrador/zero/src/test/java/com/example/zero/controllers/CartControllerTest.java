package com.example.zero.controllers;

import com.example.zero.entidades.compraCliente.DetalleCompra;
import com.example.zero.entidades.compraCliente.OrdenCompra;
import com.example.zero.entidades.persona.Cliente;
import com.example.zero.entidades.persona.Usuario;
<<<<<<< HEAD
import com.example.zero.enums.EstadoOrdenCompra;
import com.example.zero.services.OrdenCompraService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
=======
import com.example.zero.enums.RolUsuario;
import com.example.zero.services.OrdenCompraService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
>>>>>>> augusto
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
<<<<<<< HEAD
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
=======
>>>>>>> augusto
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
<<<<<<< HEAD
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
=======

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
>>>>>>> augusto
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

<<<<<<< HEAD
    private Usuario usuarioTest;
    private Cliente clienteTest;
    private OrdenCompra carritoTest;
    private List<DetalleCompra> itemsTest;

    @BeforeEach
    void setUp() {
        clienteTest = Cliente.builder().numeroDocumento("12345678").nombre("Ana").build();
        usuarioTest = Usuario.builder().id("usr-1").nombreUsuario("ana@zero.com").persona(clienteTest).build();

        DetalleCompra detalle = DetalleCompra.builder()
                .id("det-1")
                .precioUnitario(100.0)
                .cantidad(2)
                .subtotal(200.0)
                .eliminado(false)
                .build();

        itemsTest = new ArrayList<>(List.of(detalle));

        carritoTest = OrdenCompra.builder()
                .id("ord-1")
                .identificadorCompra("CART-1234")
                .estadoOrdenCompra(EstadoOrdenCompra.PENDIENTE_COMPLETAR)
                .cliente(clienteTest)
                .total(200.0)
                .eliminado(false)
                .detalles(itemsTest)
                .build();
    }

    // ==================== VIEW CART ====================

    @Test
    void viewCart_sinSesion_redirigeALogin() {
        when(session.getAttribute("usuariosession")).thenReturn(null);

        String vista = cartController.viewCart(session, model, redirectAttributes);

        assertEquals("redirect:/login", vista);
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("errorMessage"), any());
=======
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
>>>>>>> augusto
        verifyNoInteractions(ordenCompraService);
    }

    @Test
<<<<<<< HEAD
    void viewCart_conSesion_cargaCarritoYRetornaVistaCart() {
        when(session.getAttribute("usuariosession")).thenReturn(usuarioTest);
        when(ordenCompraService.obtenerOAsociarCliente(usuarioTest)).thenReturn(clienteTest);
        when(ordenCompraService.obtenerOCrearCarrito(clienteTest)).thenReturn(carritoTest);
        when(ordenCompraService.obtenerItemsActivos(carritoTest)).thenReturn(itemsTest);
        when(ordenCompraService.contarItems(carritoTest)).thenReturn(2);

        String vista = cartController.viewCart(session, model, redirectAttributes);

        assertEquals("shop/cart", vista);
        verify(model, times(1)).addAttribute("cart", carritoTest);
        verify(model, times(1)).addAttribute("items", itemsTest);
        verify(model, times(1)).addAttribute("cartCount", 2);
    }

    @Test
    void viewCart_errorAlObtenerCarrito_agregaMensajeErrorEnModelo() {
        when(session.getAttribute("usuariosession")).thenReturn(usuarioTest);
        when(ordenCompraService.obtenerOAsociarCliente(usuarioTest)).thenThrow(new RuntimeException("Fallo de BD"));

        String vista = cartController.viewCart(session, model, redirectAttributes);

        assertEquals("shop/cart", vista);
        verify(model, times(1)).addAttribute(eq("errorMessage"), contains("Fallo de BD"));
    }

    // ==================== ADD TO CART ====================

    @Test
    void addToCart_sinSesion_redirigeALogin() {
        when(session.getAttribute("usuariosession")).thenReturn(null);

        String vista = cartController.addToCart("prod-1", 2, "/shop/category", session, redirectAttributes);

        assertEquals("redirect:/login", vista);
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("errorMessage"), any());
        verifyNoInteractions(ordenCompraService);
    }

    @Test
    void addToCart_conSesion_agregaProductoYRedirigeARefererConMensajeConfirmacion() {
        when(session.getAttribute("usuariosession")).thenReturn(usuarioTest);
        when(ordenCompraService.obtenerOAsociarCliente(usuarioTest)).thenReturn(clienteTest);

        String vista = cartController.addToCart("prod-1", 2, "/shop/category", session, redirectAttributes);

        assertEquals("redirect:/shop/category", vista);
        verify(ordenCompraService, times(1)).agregarProducto(clienteTest, "prod-1", 2);
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("successMessage"), any());
    }

    @Test
    void addToCart_sinReferer_redirigeACategoryPorDefecto() {
        when(session.getAttribute("usuariosession")).thenReturn(usuarioTest);
        when(ordenCompraService.obtenerOAsociarCliente(usuarioTest)).thenReturn(clienteTest);

        String vista = cartController.addToCart("prod-1", 2, null, session, redirectAttributes);

        assertEquals("redirect:/shop/category", vista);
        verify(ordenCompraService, times(1)).agregarProducto(clienteTest, "prod-1", 2);
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("successMessage"), any());
    }

    @Test
    void addToCart_errorEnServicio_redirigeConMensajeError() {
        when(session.getAttribute("usuariosession")).thenReturn(usuarioTest);
        when(ordenCompraService.obtenerOAsociarCliente(usuarioTest)).thenReturn(clienteTest);
        doThrow(new IllegalArgumentException("Stock insuficiente"))
                .when(ordenCompraService).agregarProducto(clienteTest, "prod-1", 2);

        String vista = cartController.addToCart("prod-1", 2, "/shop/category", session, redirectAttributes);

        assertEquals("redirect:/shop/category", vista);
        verify(redirectAttributes, times(1)).addFlashAttribute("errorMessage", "Stock insuficiente");
    }

    // ==================== UPDATE CART ITEM ====================

    @Test
    void updateCartItem_sinSesion_redirigeALogin() {
        when(session.getAttribute("usuariosession")).thenReturn(null);

        String vista = cartController.updateCartItem("det-1", 5, session, redirectAttributes);

        assertEquals("redirect:/login", vista);
        verifyNoInteractions(ordenCompraService);
    }

    @Test
    void updateCartItem_conSesion_actualizaCantidadYRedirigeACart() {
        when(session.getAttribute("usuariosession")).thenReturn(usuarioTest);
        when(ordenCompraService.obtenerOAsociarCliente(usuarioTest)).thenReturn(clienteTest);

        String vista = cartController.updateCartItem("det-1", 5, session, redirectAttributes);

        assertEquals("redirect:/shop/cart", vista);
        verify(ordenCompraService, times(1)).actualizarCantidad(clienteTest, "det-1", 5);
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("successMessage"), any());
    }

    @Test
    void updateCartItem_errorEnServicio_redirigeConError() {
        when(session.getAttribute("usuariosession")).thenReturn(usuarioTest);
        when(ordenCompraService.obtenerOAsociarCliente(usuarioTest)).thenReturn(clienteTest);
        doThrow(new IllegalArgumentException("Cantidad inválida"))
                .when(ordenCompraService).actualizarCantidad(clienteTest, "det-1", 0);

        String vista = cartController.updateCartItem("det-1", 0, session, redirectAttributes);

        assertEquals("redirect:/shop/cart", vista);
        verify(redirectAttributes, times(1)).addFlashAttribute("errorMessage", "Cantidad inválida");
    }

    // ==================== REMOVE CART ITEM ====================

    @Test
    void removeCartItem_sinSesion_redirigeALogin() {
        when(session.getAttribute("usuariosession")).thenReturn(null);

        String vista = cartController.removeCartItem("det-1", session, redirectAttributes);

        assertEquals("redirect:/login", vista);
        verifyNoInteractions(ordenCompraService);
    }

    @Test
    void removeCartItem_conSesion_eliminaItemYRedirigeACart() {
        when(session.getAttribute("usuariosession")).thenReturn(usuarioTest);
        when(ordenCompraService.obtenerOAsociarCliente(usuarioTest)).thenReturn(clienteTest);

        String vista = cartController.removeCartItem("det-1", session, redirectAttributes);

        assertEquals("redirect:/shop/cart", vista);
        verify(ordenCompraService, times(1)).eliminarProducto(clienteTest, "det-1");
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("successMessage"), any());
    }

    // ==================== CLEAR CART ====================

    @Test
    void clearCart_sinSesion_redirigeALogin() {
        when(session.getAttribute("usuariosession")).thenReturn(null);

        String vista = cartController.clearCart(session, redirectAttributes);

        assertEquals("redirect:/login", vista);
        verifyNoInteractions(ordenCompraService);
    }

    @Test
    void clearCart_conSesion_vaciaCarritoYRedirigeACart() {
        when(session.getAttribute("usuariosession")).thenReturn(usuarioTest);
        when(ordenCompraService.obtenerOAsociarCliente(usuarioTest)).thenReturn(clienteTest);

        String vista = cartController.clearCart(session, redirectAttributes);

        assertEquals("redirect:/shop/cart", vista);
        verify(ordenCompraService, times(1)).vaciarCarrito(clienteTest);
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("successMessage"), any());
    }

    // ==================== COUPON ====================

    @Test
    void applyCoupon_conCodigo_agregaMensajeExito() {
        String vista = cartController.applyCoupon("PROMO2026", redirectAttributes);

        assertEquals("redirect:/shop/cart", vista);
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("successMessage"), contains("PROMO2026"));
    }

    @Test
    void applyCoupon_sinCodigo_agregaMensajeError() {
        String vista = cartController.applyCoupon("", redirectAttributes);

        assertEquals("redirect:/shop/cart", vista);
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("errorMessage"), any());
    }

    // ==================== REST API ====================

    @Test
    void getCartApi_sinSesion_retorna401() {
        when(session.getAttribute("usuariosession")).thenReturn(null);

        ResponseEntity<?> response = cartController.getCartApi(session);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void getCartApi_conSesion_retorna200() {
        when(session.getAttribute("usuariosession")).thenReturn(usuarioTest);
        when(ordenCompraService.obtenerOAsociarCliente(usuarioTest)).thenReturn(clienteTest);
        when(ordenCompraService.obtenerOCrearCarrito(clienteTest)).thenReturn(carritoTest);
        when(ordenCompraService.contarItems(carritoTest)).thenReturn(2);

        ResponseEntity<?> response = cartController.getCartApi(session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<?, ?> map = (Map<?, ?>) response.getBody();
        assertEquals("ord-1", map.get("id"));
        assertEquals(200.0, map.get("total"));
        assertEquals(2, map.get("totalItems"));
    }

    @Test
    void addToCartApi_sinSesion_retorna401() {
        when(session.getAttribute("usuariosession")).thenReturn(null);

        ResponseEntity<?> response = cartController.addToCartApi(Map.of("productId", "p-1", "quantity", 2), session);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void addToCartApi_conSesion_agregaYRetorna200() {
        when(session.getAttribute("usuariosession")).thenReturn(usuarioTest);
        when(ordenCompraService.obtenerOAsociarCliente(usuarioTest)).thenReturn(clienteTest);
        when(ordenCompraService.agregarProducto(clienteTest, "p-1", 2)).thenReturn(carritoTest);
        when(ordenCompraService.contarItems(carritoTest)).thenReturn(2);

        ResponseEntity<?> response = cartController.addToCartApi(Map.of("productId", "p-1", "quantity", 2), session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        verify(ordenCompraService, times(1)).agregarProducto(clienteTest, "p-1", 2);
    }
}
=======
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

>>>>>>> augusto
