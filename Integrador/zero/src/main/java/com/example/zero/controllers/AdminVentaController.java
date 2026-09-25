package com.example.zero.controllers;

import com.example.zero.entidades.compra.Detalle;
import com.example.zero.entidades.compra.Factura;
import com.example.zero.entidades.producto.Producto;
import com.example.zero.enums.TipoDePago;
import com.example.zero.services.producto.ProductoService;
import com.example.zero.services.VentaService;
import com.example.zero.services.persona.ClienteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlador para la gestión y registro de ventas y órdenes en el panel de administración.
 */
@Controller
public class AdminVentaController {

    private final VentaService ventaService;
    private final ProductoService productoService;
    private final ClienteService clienteService;

    public AdminVentaController(VentaService ventaService,
                                ProductoService productoService,
                                ClienteService clienteService) {
        this.ventaService = ventaService;
        this.productoService = productoService;
        this.clienteService = clienteService;
    }

    /**
     * Muestra el formulario para registrar una nueva venta.
     */
    @GetMapping({"/admin/registrar-venta", "/admin/ventas/nueva"})
    public String showRegistrarVentaForm(Model model,
                                         @RequestParam(name = "error", required = false) String error) {
        List<Producto> productos = productoService.listarActivos();
        for (Producto p : productos) {
            try {
                p.setPrecioActual(productoService.obtenerPrecioActual(p.getId()));
            } catch (Exception ignored) {
                p.setPrecioActual(0.0);
            }
        }

        model.addAttribute("productos", productos);
        model.addAttribute("clientes", clienteService.listarActivos());
        model.addAttribute("formasDePago", TipoDePago.values());

        if (error != null) {
            model.addAttribute("errorMessage", error);
        }

        return "admin/registrar-venta";
    }

    /**
     * Procesa y persiste la venta realizada desde el panel.
     */
    @PostMapping("/admin/ventas/guardar")
    public String procesarVenta(@RequestParam("clienteDni") String clienteDni,
                                @RequestParam("clienteNombre") String clienteNombre,
                                @RequestParam("clienteApellido") String clienteApellido,
                                @RequestParam(name = "clienteEmail", required = false) String clienteEmail,
                                @RequestParam(name = "formaDePago", defaultValue = "EFECTIVO") String formaDePago,
                                @RequestParam("productoIds") List<String> productoIds,
                                @RequestParam("cantidades") List<Integer> cantidades,
                                Model model) {
        try {
            ventaService.registrarVenta(clienteDni, clienteNombre, clienteApellido, clienteEmail, formaDePago, productoIds, cantidades);
            return "redirect:/admin/orders?success=created";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("clienteDni", clienteDni);
            model.addAttribute("clienteNombre", clienteNombre);
            model.addAttribute("clienteApellido", clienteApellido);
            model.addAttribute("clienteEmail", clienteEmail);
            model.addAttribute("formaDePagoSeleccionada", formaDePago);

            List<Producto> productos = productoService.listarActivos();
            for (Producto p : productos) {
                try {
                    p.setPrecioActual(productoService.obtenerPrecioActual(p.getId()));
                } catch (Exception ignored) {
                    p.setPrecioActual(0.0);
                }
            }
            model.addAttribute("productos", productos);
            model.addAttribute("clientes", clienteService.listarActivos());
            model.addAttribute("formasDePago", TipoDePago.values());
            return "admin/registrar-venta";
        }
    }

    /**
     * Listado de órdenes de venta en tables-basic.
     */
    @GetMapping({"/admin/orders", "/admin/tables-basic"})
    public String listOrders(Model model,
                             @RequestParam(name = "keyword", required = false) String keyword,
                             @RequestParam(name = "success", required = false) String success,
                             @RequestParam(name = "error", required = false) String error) {
        List<Factura> facturas = ventaService.listarVentas();
        List<Factura> orders = new ArrayList<>();

        for (Factura f : facturas) {
            if (f == null) continue;
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = keyword.trim().toLowerCase();
                boolean matchName = f.getCustomerName() != null && f.getCustomerName().toLowerCase().contains(kw);
                boolean matchOrder = f.getOrderNumber() != null && f.getOrderNumber().toLowerCase().contains(kw);
                boolean matchEmail = f.getCustomerEmail() != null && f.getCustomerEmail().toLowerCase().contains(kw);
                if (matchName || matchOrder || matchEmail) {
                    orders.add(f);
                }
            } else {
                orders.add(f);
            }
        }

        model.addAttribute("orders", orders);
        model.addAttribute("facturas", orders);
        model.addAttribute("keyword", keyword);

        if ("created".equals(success)) {
            model.addAttribute("successMessage", "Venta registrada exitosamente con su orden de compra.");
        } else if ("deleted".equals(success)) {
            model.addAttribute("successMessage", "Orden de venta eliminada exitosamente.");
        }

        if (error != null) {
            model.addAttribute("errorMessage", error);
        }

        return "admin/tables-basic";
    }

    /**
     * Baja lógica de una orden de venta.
     */
    @PostMapping("/admin/orders/delete")
    public String deleteOrder(@RequestParam("orderId") String orderId) {
        try {
            ventaService.eliminarVenta(orderId);
            return "redirect:/admin/orders?success=deleted";
        } catch (Exception e) {
            return "redirect:/admin/orders?error=" + e.getMessage();
        }
    }

    /**
     * Detalle administrativo de una factura / orden de venta.
     */
    @GetMapping({"/admin/orders/{id}", "/admin/orders/detalle/{id}"})
    public String orderDetail(@PathVariable("id") String id, Model model, RedirectAttributes redirectAttributes) {
        Factura factura = ventaService.buscarFacturaPorIdentificador(id);
        if (factura == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "No se encontró la factura u orden: " + id);
            return "redirect:/admin/orders";
        }
        model.addAttribute("factura", factura);
        model.addAttribute("order", factura);
        return "admin/order-detail";
    }
}
