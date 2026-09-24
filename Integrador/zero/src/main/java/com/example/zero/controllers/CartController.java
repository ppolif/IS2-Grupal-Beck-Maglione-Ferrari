package com.example.zero.controllers;

import com.example.zero.entidades.compraCliente.DetalleCompra;
import com.example.zero.entidades.compraCliente.OrdenCompra;
import com.example.zero.entidades.persona.Cliente;
import com.example.zero.entidades.persona.Usuario;
<<<<<<< HEAD
import com.example.zero.services.OrdenCompraService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
=======
import com.example.zero.enums.RolUsuario;
import com.example.zero.services.OrdenCompraService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
>>>>>>> augusto
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
<<<<<<< HEAD
import java.util.Map;

/**
 * Controlador para la gestión del carrito de compras (OrdenCompra PENDIENTE_COMPLETAR)
 * de clientes autenticados en el e-commerce.
 * Opera directamente con las entidades del dominio sin utilizar DTOs.
=======

/**
 * Controlador Spring MVC tradicional para la gestión del carrito de compras del cliente.
 * Se rige estrictamente por la arquitectura web tradicional y renderizado Thymeleaf sin endpoints REST ni JavaScript.
>>>>>>> augusto
 */
@Controller
@RequiredArgsConstructor
public class CartController {

    private final OrdenCompraService ordenCompraService;

    /**
<<<<<<< HEAD
     * Muestra la vista del carrito con la OrdenCompra y sus detalles del cliente autenticado.
     */
    @GetMapping({"/shop/cart", "/shop/carrito", "/cart", "/carrito"})
    public String viewCart(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuariosession");
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Debes iniciar sesión para acceder a tu carrito.");
            return "redirect:/login";
        }

=======
     * Muestra la vista principal del carrito de compras del cliente autenticado.
     */
    @GetMapping({"/shop/cart", "/cart"})
    public String showCart(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuariosession");
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Debes iniciar sesión para acceder a tu carrito de compras.");
            return "redirect:/login";
        }

        if (usuario.getRol() != RolUsuario.CLIENTE) {
            redirectAttributes.addFlashAttribute("errorMessage", "Los usuarios administradores no pueden acceder a un carrito ni operar como clientes.");
            return "redirect:/admin";
        }

>>>>>>> augusto
        try {
            Cliente cliente = ordenCompraService.obtenerOAsociarCliente(usuario);
            session.setAttribute("usuariosession", usuario);

            OrdenCompra carrito = ordenCompraService.obtenerOCrearCarrito(cliente);
            List<DetalleCompra> items = ordenCompraService.obtenerItemsActivos(carrito);
<<<<<<< HEAD
            model.addAttribute("cart", carrito);
            model.addAttribute("items", items);
            model.addAttribute("cartCount", ordenCompraService.contarItems(carrito));
=======

            model.addAttribute("cart", carrito);
            model.addAttribute("items", items);
            model.addAttribute("totalItems", ordenCompraService.contarItems(carrito));
>>>>>>> augusto
            return "shop/cart";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar el carrito: " + e.getMessage());
            return "shop/cart";
        }
    }

    /**
     * Agrega un producto al carrito de compras persistente.
<<<<<<< HEAD
     * En lugar de redirigir a la vista del carrito, mantiene al usuario en la página actual
     * y le envía un mensaje de confirmación vía flash attribute.
=======
     * Mantiene al usuario en la página de origen vía Referer y envía feedback vía Flash Attribute.
>>>>>>> augusto
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

<<<<<<< HEAD
=======
        if (usuario.getRol() != RolUsuario.CLIENTE) {
            redirectAttributes.addFlashAttribute("errorMessage", "Los usuarios administradores no pueden agregar productos al carrito ni operar como clientes.");
            if (referer != null && !referer.isBlank()) {
                return "redirect:" + referer;
            }
            return "redirect:/admin";
        }

>>>>>>> augusto
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

<<<<<<< HEAD
=======
        if (usuario.getRol() != RolUsuario.CLIENTE) {
            redirectAttributes.addFlashAttribute("errorMessage", "Los usuarios administradores no pueden operar sobre el carrito.");
            return "redirect:/admin";
        }

>>>>>>> augusto
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

<<<<<<< HEAD
=======
        if (usuario.getRol() != RolUsuario.CLIENTE) {
            redirectAttributes.addFlashAttribute("errorMessage", "Los usuarios administradores no pueden operar sobre el carrito.");
            return "redirect:/admin";
        }

>>>>>>> augusto
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
<<<<<<< HEAD
     * Vacía todos los ítems del carrito de compras.
=======
     * Vacía todos los ítems del carrito de compras activo.
>>>>>>> augusto
     */
    @PostMapping({"/shop/cart/clear", "/cart/clear"})
    public String clearCart(HttpSession session, RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuariosession");
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Debes iniciar sesión para vaciar el carrito.");
            return "redirect:/login";
        }

<<<<<<< HEAD
=======
        if (usuario.getRol() != RolUsuario.CLIENTE) {
            redirectAttributes.addFlashAttribute("errorMessage", "Los usuarios administradores no pueden operar sobre el carrito.");
            return "redirect:/admin";
        }

>>>>>>> augusto
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
<<<<<<< HEAD
                              RedirectAttributes redirectAttributes) {
=======
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuariosession");
        if (usuario != null && usuario.getRol() != RolUsuario.CLIENTE) {
            redirectAttributes.addFlashAttribute("errorMessage", "Los usuarios administradores no pueden operar sobre el carrito.");
            return "redirect:/admin";
        }

>>>>>>> augusto
        if (couponCode == null || couponCode.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ingresa un código de cupón válido.");
        } else {
            redirectAttributes.addFlashAttribute("successMessage", "Cupón '" + couponCode.trim() + "' aplicado exitosamente.");
        }
        return "redirect:/shop/cart";
    }
<<<<<<< HEAD

    /**
     * Endpoint API para consultar el estado del carrito en formato JSON.
     */
    @GetMapping("/api/cart")
    @ResponseBody
    public ResponseEntity<?> getCartApi(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuariosession");
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No autenticado"));
        }

        try {
            Cliente cliente = ordenCompraService.obtenerOAsociarCliente(usuario);
            OrdenCompra carrito = ordenCompraService.obtenerOCrearCarrito(cliente);
            return ResponseEntity.ok(Map.of(
                    "id", carrito.getId() != null ? carrito.getId() : "",
                    "total", carrito.getTotal(),
                    "totalItems", ordenCompraService.contarItems(carrito)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint API para agregar productos al carrito vía AJAX / REST.
     */
    @PostMapping("/api/cart/add")
    @ResponseBody
    public ResponseEntity<?> addToCartApi(@RequestBody Map<String, Object> payload, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuariosession");
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No autenticado"));
        }

        String productId = (String) payload.get("productId");
        int quantity = payload.containsKey("quantity") ? ((Number) payload.get("quantity")).intValue() : 1;

        try {
            Cliente cliente = ordenCompraService.obtenerOAsociarCliente(usuario);
            OrdenCompra carrito = ordenCompraService.agregarProducto(cliente, productId, quantity);
            return ResponseEntity.ok(Map.of(
                    "id", carrito.getId() != null ? carrito.getId() : "",
                    "total", carrito.getTotal(),
                    "totalItems", ordenCompraService.contarItems(carrito)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
=======
}

>>>>>>> augusto
