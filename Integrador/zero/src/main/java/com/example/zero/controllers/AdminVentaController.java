package com.example.zero.controllers;

import com.example.zero.dto.OrderViewDto;
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
        List<OrderViewDto> orders = new ArrayList<>();

        for (Factura f : facturas) {
            OrderViewDto dto = mapearFacturaAOrderDto(f);
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = keyword.trim().toLowerCase();
                boolean matchName = dto.getCustomerName() != null && dto.getCustomerName().toLowerCase().contains(kw);
                boolean matchOrder = dto.getOrderNumber() != null && dto.getOrderNumber().toLowerCase().contains(kw);
                boolean matchEmail = dto.getCustomerEmail() != null && dto.getCustomerEmail().toLowerCase().contains(kw);
                if (matchName || matchOrder || matchEmail) {
                    orders.add(dto);
                }
            } else {
                orders.add(dto);
            }
        }

        model.addAttribute("orders", orders);
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
     * Mapeo de Factura a DTO para vistas de administración y comprobantes.
     */
    public OrderViewDto mapearFacturaAOrderDto(Factura f) {
        String clientName = f.getCliente() != null
                ? (f.getCliente().getNombre() + " " + f.getCliente().getApellido()).trim()
                : "Cliente General";
        String email = (f.getCliente() != null && f.getCliente().getUsuario() != null)
                ? f.getCliente().getUsuario().getNombreUsuario()
                : (f.getCliente() != null ? "DNI: " + f.getCliente().getNumeroDocumento() : "N/A");

        StringBuilder summary = new StringBuilder();
        String category = "General";
        List<OrderViewDto.OrderItemDto> items = new ArrayList<>();

        if (f.getDetalles() != null) {
            for (Detalle d : f.getDetalles()) {
                if (d.getProducto() != null) {
                    if (summary.length() > 0) summary.append(", ");
                    summary.append(d.getProducto().getNombre()).append(" (x").append(d.getCantidad()).append(")");
                    if (d.getProducto().getSubCategoria() != null && d.getProducto().getSubCategoria().getCategoria() != null) {
                        category = d.getProducto().getSubCategoria().getCategoria().getNombre();
                    }
                    items.add(OrderViewDto.OrderItemDto.builder()
                            .productName(d.getProducto().getNombre())
                            .quantity(d.getCantidad())
                            .unitPrice(d.getCantidad() > 0 ? Math.round((d.getSubtotal() / d.getCantidad()) * 100.0) / 100.0 : 0.0)
                            .totalPrice(d.getSubtotal())
                            .build());
                }
            }
        }

        String payment = (f.getFormaDePago() != null && f.getFormaDePago().getTipoPago() != null)
                ? f.getFormaDePago().getTipoPago().name().replace('_', ' ')
                : "Efectivo";

        return OrderViewDto.builder()
                .id(f.getId())
                .orderNumber("#ORD-" + f.getNumeroFactura())
                .numeroFactura(f.getNumeroFactura())
                .customerName(clientName)
                .customerEmail(email)
                .customerPhone("+54 11 0000-0000")
                .productSummary(summary.length() > 0 ? summary.toString() : "Venta General")
                .categoryName(category)
                .totalAmount(f.getTotalPagado())
                .subtotal(f.getTotalPagado())
                .status("Completado")
                .paymentMethod(payment)
                .createdAt(f.getFechaFactura())
                .shippingAddress("Mostrador / Entrega Inmediata")
                .shippingCity("Sucursal Central")
                .shippingZip("C1000")
                .items(items)
                .build();
    }
}

