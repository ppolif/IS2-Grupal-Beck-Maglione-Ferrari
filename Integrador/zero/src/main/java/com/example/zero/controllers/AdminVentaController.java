package com.example.zero.controllers;

import com.example.zero.entidades.compra.Detalle;
import com.example.zero.entidades.compra.Factura;
import com.example.zero.entidades.producto.Producto;
import com.example.zero.enums.TipoDePago;
import com.example.zero.services.producto.ProductoService;
import com.example.zero.services.VentaService;
import com.example.zero.services.persona.ClienteService;
import com.example.zero.services.StockService;
import com.example.zero.entidades.compraProveedor.FacturaProveedor;
import com.example.zero.entidades.empresa.Empresa;
import com.example.zero.enums.TipoEmpresa;
import com.example.zero.services.EmpresaService;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final StockService stockService;
    private final EmpresaService empresaService;

    public AdminVentaController(VentaService ventaService,
                                ProductoService productoService,
                                ClienteService clienteService,
                                StockService stockService) {
        this(ventaService, productoService, clienteService, stockService, null);
    }

    @Autowired
    public AdminVentaController(VentaService ventaService,
                                ProductoService productoService,
                                ClienteService clienteService,
                                StockService stockService,
                                @Autowired(required = false) EmpresaService empresaService) {
        this.ventaService = ventaService;
        this.productoService = productoService;
        this.clienteService = clienteService;
        this.stockService = stockService;
        this.empresaService = empresaService;
    }

    /**
     * Muestra el formulario para registrar una nueva venta.
     */
    @GetMapping({"/admin/registrar-venta", "/admin/ventas/nueva"})
    public String showRegistrarVentaForm(Model model,
                                         @RequestParam(name = "error", required = false) String error) {
        List<Producto> productos = productoService.listarActivos();
        List<com.example.zero.dto.producto.ProductoDTO> dtos = new ArrayList<>();
        for (Producto p : productos) {
            double precio = 0.0;
            try {
                precio = productoService.obtenerPrecioActual(p.getId());
            } catch (Exception ignored) {
            }
            int stock = (stockService != null) ? stockService.calcularStockActual(p.getId()) : 0;
            dtos.add(com.example.zero.dto.producto.ProductoDTO.de(p, precio, stock, stockService));
        }

        model.addAttribute("productos", dtos);
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
            List<com.example.zero.dto.producto.ProductoDTO> dtos = new ArrayList<>();
            for (Producto p : productos) {
                double precio = 0.0;
                try {
                    precio = productoService.obtenerPrecioActual(p.getId());
                } catch (Exception ignored) {
                }
                int stock = (stockService != null) ? stockService.calcularStockActual(p.getId()) : 0;
                dtos.add(com.example.zero.dto.producto.ProductoDTO.de(p, precio, stock, stockService));
            }
            model.addAttribute("productos", dtos);
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
     * Detalle administrativo de una factura (venta a cliente o compra a proveedor).
     */
    @GetMapping({"/admin/orders/{id}", "/admin/orders/detalle/{id}"})
    public String orderDetail(@PathVariable("id") String id, Model model, RedirectAttributes redirectAttributes) {
        Factura factura = ventaService.buscarFacturaPorIdentificador(id);
        if (factura == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "No se encontró la factura u orden: " + id);
            return "redirect:/admin/orders";
        }

        boolean esCompraProveedor = factura instanceof FacturaProveedor;
        model.addAttribute("factura", factura);
        model.addAttribute("order", factura);
        model.addAttribute("esCompraProveedor", esCompraProveedor);

        if (esCompraProveedor) {
            FacturaProveedor fp = (FacturaProveedor) factura;
            model.addAttribute("facturaProveedor", fp);
            model.addAttribute("proveedor", fp.getProveedor());
            Empresa sucursal = (empresaService != null) ? empresaService.obtenerSucursalActiva() : null;
            if (sucursal == null) {
                sucursal = new Empresa();
                sucursal.setId("SUC-001");
                sucursal.setRazonSocial("ZERO Argentina S.A. - Sucursal Central");
                sucursal.setCuit("30-71829384-9");
                sucursal.setTipoSucursal(TipoEmpresa.SEDE_CENTRAL);
            }
            model.addAttribute("sucursal", sucursal);
            String dirCompleta = (empresaService != null) ? empresaService.obtenerDireccionCompleta(sucursal) : "Av. Corrientes 1234, CABA";
            model.addAttribute("sucursalDireccion", dirCompleta);
        }

        return "admin/order-detail";
    }
}
