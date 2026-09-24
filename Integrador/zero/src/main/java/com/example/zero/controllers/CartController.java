package com.example.zero.controllers;

import com.example.zero.entidades.compraCliente.DetalleCompra;
import com.example.zero.entidades.compraCliente.OrdenCompra;
import com.example.zero.entidades.persona.Cliente;
import com.example.zero.entidades.persona.Usuario;
import com.example.zero.services.OrdenCompraService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controlador Spring MVC tradicional para la gestión del carrito de compras del cliente.
 * Se rige estrictamente por la arquitectura web tradicional y renderizado Thymeleaf sin endpoints REST ni JavaScript.
 */
@Controller
@RequiredArgsConstructor
public class CartController {

    private final OrdenCompraService ordenCompraService;

    /**
     * Muestra la vista principal del carrito de compras del cliente autenticado.
     */
    @GetMapping({"/shop/cart", "/cart"})
    public String showCart(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuariosession");
        if (usuario == null) {
            model.addAttribute("errorMessage", "Debes iniciar sesión para acceder a tu carrito de compras.");
            return "redirect:/login";
        }

        try {
            Cliente cliente = ordenCompraService.obtenerOAsociarCliente(usuario);
            session.setAttribute("usuariosession", usuario);

            OrdenCompra carrito = ordenCompraService.obtenerOCrearCarrito(cliente);
            List<DetalleCompra> items = ordenCompraService.obtenerItemsActivos(carrito);

            model.addAttribute("cart", carrito);
            model.addAttribute("items", items);
            model.addAttribute("totalItems", ordenCompraService.contarItems(carrito));
            return "shop/cart";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar el carrito: " + e.getMessage());
            return "shop/cart";
        }
    }

    /**
     * Agrega un producto al carrito de compras persistente.
     * Mantiene al usuario en la página de origen vía Referer y envía feedback vía Flash Attribute.
     */
    @PostMapping({"/shop/cart/add", "/cart/add"})
    public String addToCart(@RequestParam("productId") String productId,
                            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                            @RequestHeader(value = "Referer", required = false) String referer,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuariosession");
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Debes iniciar sesión para agregar productos al carrito.");
            return "redirect:/login";
        }

        try {
            Cliente cliente = ordenCompraService.obtenerOAsociarCliente(usuario);
            session.setAttribute("usuariosession", usuario);

            ordenCompraService.agregarProducto(cliente, productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "¡Producto agregado al carrito con éxito!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        if (referer != null && !referer.isBlank()) {
            return "redirect:" + referer;
        }
        return "redirect:/shop/category";
    }

    /**
     * Actualiza la cantidad de un ítem en el carrito.
     */
    @PostMapping({"/shop/cart/update", "/cart/update"})
    public String updateCartItem(@RequestParam("itemId") String itemId,
                                 @RequestParam("quantity") int quantity,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuariosession");
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Debes iniciar sesión para actualizar el carrito.");
            return "redirect:/login";
        }

        try {
            Cliente cliente = ordenCompraService.obtenerOAsociarCliente(usuario);
            session.setAttribute("usuariosession", usuario);

            ordenCompraService.actualizarCantidad(cliente, itemId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Carrito actualizado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/shop/cart";
    }

    /**
     * Elimina un ítem específico del carrito de compras.
     */
    @PostMapping({"/shop/cart/remove", "/cart/remove"})
    public String removeCartItem(@RequestParam("itemId") String itemId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuariosession");
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Debes iniciar sesión para operar con el carrito.");
            return "redirect:/login";
        }

        try {
            Cliente cliente = ordenCompraService.obtenerOAsociarCliente(usuario);
            session.setAttribute("usuariosession", usuario);

            ordenCompraService.eliminarProducto(cliente, itemId);
            redirectAttributes.addFlashAttribute("successMessage", "Producto eliminado del carrito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/shop/cart";
    }

    /**
     * Vacía todos los ítems del carrito de compras activo.
     */
    @PostMapping({"/shop/cart/clear", "/cart/clear"})
    public String clearCart(HttpSession session, RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuariosession");
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Debes iniciar sesión para vaciar el carrito.");
            return "redirect:/login";
        }

        try {
            Cliente cliente = ordenCompraService.obtenerOAsociarCliente(usuario);
            session.setAttribute("usuariosession", usuario);

            ordenCompraService.vaciarCarrito(cliente);
            redirectAttributes.addFlashAttribute("successMessage", "El carrito ha sido vaciado.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/shop/cart";
    }

    /**
     * Aplica un cupón de descuento en el carrito.
     */
    @PostMapping({"/shop/cart/coupon", "/cart/coupon"})
    public String applyCoupon(@RequestParam(value = "couponCode", required = false) String couponCode,
                              RedirectAttributes redirectAttributes) {
        if (couponCode == null || couponCode.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ingresa un código de cupón válido.");
        } else {
            redirectAttributes.addFlashAttribute("successMessage", "Cupón '" + couponCode.trim() + "' aplicado exitosamente.");
        }
        return "redirect:/shop/cart";
    }
}

