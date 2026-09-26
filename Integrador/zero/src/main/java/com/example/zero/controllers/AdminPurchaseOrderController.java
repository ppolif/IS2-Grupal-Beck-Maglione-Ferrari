package com.example.zero.controllers;

import com.example.zero.entidades.compraProveedor.FacturaProveedor;
import com.example.zero.entidades.compraProveedor.Proveedor;
import com.example.zero.entidades.producto.Producto;
import com.example.zero.enums.EstadoFactura;
import com.example.zero.enums.TipoDePago;
import com.example.zero.services.CompraProveedorService;
import com.example.zero.services.ProveedorService;
import com.example.zero.services.producto.ProductoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador de Administración para gestionar Órdenes de Compra a Proveedores (FacturaProveedor).
 */
@Controller
public class AdminPurchaseOrderController {

    private final CompraProveedorService compraProveedorService;
    private final ProveedorService proveedorService;
    private final ProductoService productoService;

    public AdminPurchaseOrderController(CompraProveedorService compraProveedorService,
                                        ProveedorService proveedorService,
                                        ProductoService productoService) {
        this.compraProveedorService = compraProveedorService;
        this.proveedorService = proveedorService;
        this.productoService = productoService;
    }

    /**
     * Formulario de registro de orden de compra / FacturaProveedor y listado de órdenes registradas.
     */
    @GetMapping({"/admin/registrar-compra", "/admin/compras/nueva", "/admin/purchase-orders", "/admin/purchase-orders/nueva"})
    public String showRegistrarCompraForm(Model model,
                                          @RequestParam(name = "success", required = false) String success,
                                          @RequestParam(name = "error", required = false) String error) {
        cargarDatosModelo(model);

        if ("created".equals(success)) {
            model.addAttribute("successMessage", "Orden de compra a proveedor registrada exitosamente y stock incrementado.");
        } else if ("deleted".equals(success)) {
            model.addAttribute("successMessage", "Orden de compra a proveedor eliminada exitosamente.");
        }

        if (error != null) {
            model.addAttribute("errorMessage", error);
        }

        return "admin/registrar-compra";
    }

    /**
     * Procesa y persiste la FacturaProveedor con sus detalles de compra.
     * Número de factura y fecha de factura se generan automáticamente si se omiten.
     */
    @PostMapping({"/admin/compras/guardar", "/admin/purchase-orders"})
    public String registrarCompra(@RequestParam("proveedorId") String proveedorId,
                                  @RequestParam(name = "numeroFactura", required = false) Long numeroFactura,
                                  @RequestParam(name = "fechaFactura", required = false)
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFactura,
                                  @RequestParam(name = "formaDePago", defaultValue = "TRANSFERENCIA") String formaDePago,
                                  @RequestParam(name = "estado", defaultValue = "PAGADA") String estado,
                                  @RequestParam("productoIds") List<String> productoIds,
                                  @RequestParam("cantidades") List<Integer> cantidades,
                                  @RequestParam(name = "costosUnitarios", required = false) List<Double> costosUnitarios,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        try {
            LocalDateTime fecha = (fechaFactura != null) ? fechaFactura : LocalDateTime.now();
            compraProveedorService.registrarCompraProveedor(
                    proveedorId,
                    numeroFactura,
                    fecha,
                    formaDePago,
                    estado,
                    productoIds,
                    cantidades,
                    costosUnitarios
            );
            redirectAttributes.addFlashAttribute("successMessage", "Orden de compra a proveedor registrada exitosamente. Se ha incrementado el stock de los productos.");
            return "redirect:/admin/registrar-compra?success=created";
        } catch (Exception e) {
            cargarDatosModelo(model);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("selectedProveedorId", proveedorId);
            model.addAttribute("selectedFormaDePago", formaDePago);
            model.addAttribute("selectedEstado", estado);
            return "admin/registrar-compra";
        }
    }

    /**
     * Sobrecarga sin numeroFactura ni fechaFactura.
     */
    public String registrarCompra(String proveedorId,
                                  String formaDePago,
                                  String estado,
                                  List<String> productoIds,
                                  List<Integer> cantidades,
                                  List<Double> costosUnitarios,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        return registrarCompra(proveedorId, null, null, formaDePago, estado, productoIds, cantidades, costosUnitarios, model, redirectAttributes);
    }

    /**
     * Baja lógica de una orden de compra a proveedor.
     */
    @PostMapping("/admin/purchase-orders/eliminar/{id}")
    public String eliminarCompra(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        try {
            compraProveedorService.eliminarCompraProveedor(id);
            return "redirect:/admin/registrar-compra?success=deleted";
        } catch (Exception e) {
            return "redirect:/admin/registrar-compra?error=" + e.getMessage();
        }
    }

    private void cargarDatosModelo(Model model) {
        List<Proveedor> proveedores = proveedorService.listarActivos();
        if (proveedores == null || proveedores.isEmpty()) {
            proveedores = proveedorService.listarTodos();
        }

        List<Producto> productos = productoService.listarActivos();
        if (productos == null || productos.isEmpty()) {
            productos = productoService.listarTodos();
        }

        for (Producto p : productos) {
            try {
                p.setPrecioActual(productoService.obtenerPrecioActual(p.getId()));
            } catch (Exception ignored) {
                p.setPrecioActual(0.0);
            }
        }
        List<FacturaProveedor> compras = compraProveedorService.listarComprasProveedor();

        model.addAttribute("proveedores", proveedores != null ? proveedores : java.util.Collections.emptyList());
        model.addAttribute("productos", productos != null ? productos : java.util.Collections.emptyList());
        model.addAttribute("formasDePago", TipoDePago.values());
        model.addAttribute("estadosFactura", EstadoFactura.values());
        model.addAttribute("compras", compras != null ? compras : java.util.Collections.emptyList());
    }
}
