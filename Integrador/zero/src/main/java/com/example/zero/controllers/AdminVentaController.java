package com.example.zero.controllers;

import com.example.zero.dto.producto.ProductoDTO;
import com.example.zero.entidades.compra.Detalle;
import com.example.zero.entidades.compra.Factura;
import com.example.zero.entidades.producto.Producto;
import com.example.zero.enums.TipoDePago;
import com.example.zero.services.ContactoService;
import com.example.zero.services.producto.ProductoService;
import com.example.zero.services.VentaService;
import com.example.zero.services.persona.ClienteService;
import com.example.zero.services.StockService;
import com.example.zero.entidades.compraProveedor.FacturaProveedor;
import com.example.zero.entidades.empresa.Empresa;
import com.example.zero.entidades.persona.Cliente;
import com.example.zero.enums.TipoEmpresa;
import com.example.zero.services.EmpresaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

///controlador para registrar ventas en local
@Controller
public class AdminVentaController {

    private final VentaService ventaService;
    private final ProductoService productoService;
    private final ClienteService clienteService;
    private final StockService stockService;
    private final EmpresaService empresaService;
    private final ContactoService contactoService;


    @Autowired
    public AdminVentaController(VentaService ventaService,
                                ProductoService productoService,
                                ClienteService clienteService,
                                StockService stockService,
                                @Autowired(required = false) EmpresaService empresaService,
                                @Autowired(required = false) ContactoService contactoService) {
        this.ventaService = ventaService;
        this.productoService = productoService;
        this.clienteService = clienteService;
        this.stockService = stockService;
        this.empresaService = empresaService;
        this.contactoService = contactoService;
    }

    ///formulariooooooo
    @GetMapping({"/admin/registrar-venta", "/admin/ventas/nueva"})
    public String showRegistrarVentaForm(Model model,
                                         @RequestParam(name = "error", required = false) String error) {
        List<Producto> productos = productoService.listarActivos();
        List<ProductoDTO> dtos = new ArrayList<>();
        for (Producto p : productos) {
            double precio = 0.0;
            try {
                precio = productoService.obtenerPrecioActual(p.getId());
            } catch (Exception ignored) {
            }
            int stock = stockService.calcularStockActual(p.getId());
            dtos.add(ProductoDTO.de(p, precio, stock, stockService, productoService));
        }

        model.addAttribute("productos", dtos);
        model.addAttribute("clientes", clienteService.listarActivos());
        model.addAttribute("formasDePago", TipoDePago.values());

        if (error != null) {
            model.addAttribute("errorMessage", error);
        }

        return "admin/registrar-venta";
    }

    ///procesa y persiste la venta
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
            List<ProductoDTO> dtos = new ArrayList<>();
            for (Producto p : productos) {
                double precio = 0.0;
                try {
                    precio = productoService.obtenerPrecioActual(p.getId());
                } catch (Exception ignored) {
                }
                int stock = stockService.calcularStockActual(p.getId());
                dtos.add(ProductoDTO.de(p, precio, stock, stockService, productoService));
            }
            model.addAttribute("productos", dtos);
            model.addAttribute("clientes", clienteService.listarActivos());
            model.addAttribute("formasDePago", TipoDePago.values());
            return "admin/registrar-venta";
        }
    }

    ///listado en tabla de ordenes
    @GetMapping({"/admin/orders"})
    public String listOrders(Model model,
                             @RequestParam(name = "keyword", required = false) String keyword,
                             @RequestParam(name = "success", required = false) String success,
                             @RequestParam(name = "error", required = false) String error) {
        List<Factura> facturas = ventaService.listarVentas();
        List<Factura> orders = new ArrayList<>();

        //este codigo es para buscar facturas (filtro)
        for (Factura f : facturas) {
            if (f == null) continue;
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = keyword.trim().toLowerCase();
                String nombreComp = ventaService.obtenerNombreComprobante(f);
                String emailComp = ventaService.obtenerEmailComprobante(f);
                String numFacturaStr = f.getNumeroFactura() != null ? "#ORD-" + f.getNumeroFactura() : f.getId();

                boolean matchName = nombreComp != null && nombreComp.toLowerCase().contains(kw);
                boolean matchOrder = numFacturaStr != null && numFacturaStr.toLowerCase().contains(kw);
                boolean matchEmail = emailComp != null && emailComp.toLowerCase().contains(kw);
                if (matchName || matchOrder || matchEmail) {
                    orders.add(f);
                }
            } else {
                orders.add(f);
            }
        }

        Map<String, String> imagenesPorOrder = new HashMap<>();
        for (Factura f : orders) {
            if (f != null && f.getId() != null) {
                String imgUrl = "/admin/assets/images/avatar.png";
                if (ventaService != null) {
                    try {
                        String foto = ventaService.obtenerFotoComprobante(f);
                        if (foto != null && !foto.isBlank()) {
                            imgUrl = foto;
                        }
                    } catch (Exception ignored) {
                    }
                }
                imagenesPorOrder.put(f.getId(), imgUrl);
            }
        }

        model.addAttribute("orders", orders);
        model.addAttribute("facturas", orders);
        model.addAttribute("keyword", keyword);
        model.addAttribute("imagenesPorOrder", imagenesPorOrder);
        model.addAttribute("ventaService", ventaService);

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


    @PostMapping("/admin/orders/delete")
    public String deleteOrder(@RequestParam("orderId") String orderId) {
        try {
            ventaService.eliminarVenta(orderId);
            return "redirect:/admin/orders?success=deleted";
        } catch (Exception e) {
            return "redirect:/admin/orders?error=" + e.getMessage();
        }
    }

    ///detalle de una orden
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

        String customerName = ventaService.obtenerNombreComprobante(factura);
        String customerEmail = ventaService.obtenerEmailComprobante(factura);
        String productSummary = ventaService.obtenerResumenProductos(factura);
        Cliente cliente = ventaService.obtenerClienteDeFactura(factura);

        String customerPhone = "";
        String shippingAddress = "";
        if (cliente != null) {
            customerPhone = contactoService.obtenerTelefonoPrincipal(cliente).orElse("");
        }

        String clienteFotoUrl = "/admin/assets/images/avatar.png";
        try {
            String foto = ventaService.obtenerFotoComprobante(factura);
            if (foto != null && !foto.isBlank()) {
                clienteFotoUrl = foto;
            }
        } catch (Exception ignored) {
        }

        Map<String, String> imagenesPorOrder = new HashMap<>();
        if (factura.getId() != null) {
            imagenesPorOrder.put(factura.getId(), clienteFotoUrl);
        }

        model.addAttribute("cliente", cliente);
        model.addAttribute("customerName", customerName);
        model.addAttribute("customerEmail", customerEmail);
        model.addAttribute("customerPhone", customerPhone);
        model.addAttribute("shippingAddress", shippingAddress);
        model.addAttribute("productSummary", productSummary);
        model.addAttribute("clienteFotoUrl", clienteFotoUrl);
        model.addAttribute("imagenesPorOrder", imagenesPorOrder);
        model.addAttribute("ventaService", ventaService);

        if (esCompraProveedor) {
            FacturaProveedor fp = (FacturaProveedor) factura;
            model.addAttribute("facturaProveedor", fp);
            model.addAttribute("proveedor", fp.getProveedor());
            Empresa sucursal = empresaService.obtenerSucursalActiva();

            //hardcodeamos la empresa porque no tenemos alta jaja
            if (sucursal == null) {
                sucursal = new Empresa();
                sucursal.setId("SUC-001");
                sucursal.setRazonSocial("ZERO Argentina S.A. - Sucursal Central");
                sucursal.setCuit("30-71829384-9");
                sucursal.setTipoSucursal(TipoEmpresa.SEDE_CENTRAL);
            }
            model.addAttribute("sucursal", sucursal);
            String dirCompleta = empresaService.obtenerDireccionCompleta(sucursal);
            model.addAttribute("sucursalDireccion", dirCompleta);
        }

        return "admin/order-detail";
    }
}
