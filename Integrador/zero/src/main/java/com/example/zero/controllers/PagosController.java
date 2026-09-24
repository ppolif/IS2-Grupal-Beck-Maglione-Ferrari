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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador para la integración del flujo de pagos con Mercado Pago.
 * Maneja la creación de la preferencia desde el carrito, la redirección hacia la pasarela
 * y el retorno exitoso de la transacción.
 */
@Controller
@RequiredArgsConstructor
public class PagosController {

    private final MercadoPagoService mercadoPagoService;
    private final OrdenCompraService ordenCompraService;

    /**
     * Inicia el proceso de pago con Mercado Pago para el carrito de compras activo.
     * Crea la preferencia de pago en Mercado Pago y redirige al cliente a la pantalla oficial de cobro.
     */
    @RequestMapping(value = {"/api/mercadoPago", "/checkout/mercadopago", "/shop/checkout/mercadopago"}, method = {RequestMethod.GET, RequestMethod.POST})
    public String mercadoPago(HttpSession session,
                              HttpServletRequest request,
                              RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuariosession");
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Debes iniciar sesión para abonar tu compra.");
            return "redirect:/login";
        }

        if (usuario.getRol() != RolUsuario.CLIENTE) {
            redirectAttributes.addFlashAttribute("errorMessage", "Solo los clientes pueden realizar compras en la tienda.");
            return "redirect:/admin";
        }

        try {
            Cliente cliente = ordenCompraService.obtenerOAsociarCliente(usuario);
            session.setAttribute("usuariosession", usuario);

            OrdenCompra carrito = ordenCompraService.obtenerOCrearCarrito(cliente);
            List<DetalleCompra> items = ordenCompraService.obtenerItemsActivos(carrito);

            if (items == null || items.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Tu carrito de compras está vacío.");
                return "redirect:/shop/cart";
            }

            // Construir URL base dinámica respetando proxies / tunnels (ngrok) si existen
            String scheme = request.getHeader("X-Forwarded-Proto") != null
                    ? request.getHeader("X-Forwarded-Proto")
                    : request.getScheme();
            String host = request.getHeader("X-Forwarded-Host") != null
                    ? request.getHeader("X-Forwarded-Host")
                    : (request.getServerName() + ((request.getServerPort() == 80 || request.getServerPort() == 443) ? "" : ":" + request.getServerPort()));
            String contextPath = request.getContextPath() != null ? request.getContextPath() : "";
            String baseUrl = scheme + "://" + host + contextPath;

            String redirectUrl = mercadoPagoService.crearPreferenciaParaCarrito(carrito, cliente, baseUrl);
            return "redirect:" + redirectUrl;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al conectar con Mercado Pago: " + e.getMessage());
            return "redirect:/shop/cart";
        }
    }

    /**
     * Endpoint REST para consultar la URL de la pasarela si se requiere vía llamada asíncrona.
     */
    @GetMapping(value = "/api/mercadoPago/url")
    @ResponseBody
    public Map<String, String> obtenerUrlMercadoPago(HttpSession session, HttpServletRequest request) {
        Map<String, String> response = new HashMap<>();
        Usuario usuario = (Usuario) session.getAttribute("usuariosession");
        if (usuario == null || usuario.getRol() != RolUsuario.CLIENTE) {
            response.put("error", "Usuario no autorizado");
            return response;
        }

        try {
            Cliente cliente = ordenCompraService.obtenerOAsociarCliente(usuario);
            OrdenCompra carrito = ordenCompraService.obtenerOCrearCarrito(cliente);

            String scheme = request.getHeader("X-Forwarded-Proto") != null
                    ? request.getHeader("X-Forwarded-Proto")
                    : request.getScheme();
            String host = request.getHeader("X-Forwarded-Host") != null
                    ? request.getHeader("X-Forwarded-Host")
                    : (request.getServerName() + ((request.getServerPort() == 80 || request.getServerPort() == 443) ? "" : ":" + request.getServerPort()));
            String contextPath = request.getContextPath() != null ? request.getContextPath() : "";
            String baseUrl = scheme + "://" + host + contextPath;

            String initPoint = mercadoPagoService.crearPreferenciaParaCarrito(carrito, cliente, baseUrl);
            response.put("url", initPoint);
            return response;
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return response;
        }
    }

    /**
     * Retorno exitoso de Mercado Pago tras un pago aprobado.
     * Registra la venta y redirige a la página de confirmación con los detalles del pedido.
     */
    @GetMapping({"/checkout/success", "/api/mercadoPago/success", "/shop/checkout/success"})
    public String pagoExitoso(@RequestParam(name = "payment_id", required = false) String paymentId,
                              @RequestParam(name = "status", required = false) String status,
                              @RequestParam(name = "external_reference", required = false) String externalReference,
                              @RequestParam(name = "collection_status", required = false) String collectionStatus,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuariosession");
        Cliente cliente = null;
        if (usuario != null && usuario.getRol() == RolUsuario.CLIENTE) {
            try {
                cliente = ordenCompraService.obtenerOAsociarCliente(usuario);
            } catch (Exception ignored) {
            }
        }

        try {
            Factura factura = mercadoPagoService.procesarPagoExitoso(externalReference, paymentId, cliente);
            redirectAttributes.addFlashAttribute("successMessage", "¡Tu pago con Mercado Pago ha sido procesado exitosamente!");
            return "redirect:/shop/confirmation?orderNumber=" + factura.getNumeroFactura();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar la confirmación: " + e.getMessage());
            return "redirect:/shop/confirmation";
        }
    }
}
