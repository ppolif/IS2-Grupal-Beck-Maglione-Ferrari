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
import java.util.ArrayList;
import java.util.List;

//abm de ordenes de compra a proveedores
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

    //formulario
    @GetMapping({"/admin/registrar-compra", "/admin/compras/nueva", "/admin/purchase-orders", "/admin/purchase-orders/nueva"})
    public String showRegistrarCompraForm(Model model,
                                          @RequestParam(name = "success", required = false) String success,
                                          @RequestParam(name = "error", required = false) String error) {
        cargarDatosModelo(model);

        if ("created".equals(success)) {
            model.addAttribute("successMessage", "Orden de compra a proveedor registrada exitosamente.");
        } else if ("deleted".equals(success)) {
            model.addAttribute("successMessage", "Orden de compra a proveedor eliminada exitosamente.");
        }

        if (error != null) {
            model.addAttribute("errorMessage", error);
        }

        return "admin/registrar-compra";
    }

    ///
    @PostMapping({"/admin/compras/guardar", "/admin/purchase-orders"})
    public String registrarCompra(@RequestParam("proveedorId") String proveedorId,
                                  @RequestParam(name = "numeroFactura", required = false) Long numeroFactura,
                                  @RequestParam(name = "fechaFactura", required = false)
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFactura,
                                  @RequestParam(name = "formaDePago", defaultValue = "TRANSFERENCIA") String formaDePago,
                                  @RequestParam(name = "estado", defaultValue = "SIN_DEFINIR") String estado,
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
            String successMsg = "ENTREGADA".equalsIgnoreCase(estado != null ? estado.trim() : "")
                    ? "Orden de compra a proveedor registrada exitosamente. Se ha incrementado el stock de los productos."
                    : "Orden de compra a proveedor registrada exitosamente.";
            redirectAttributes.addFlashAttribute("successMessage", successMsg);
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


    ///busca el ultimo precio al que un proveedor cobro un producto para mostrarlo en el formulario
    @GetMapping("/admin/compras/costo-unitario")
    public String obtenerCostoUnitario(
            @RequestParam(value = "proveedorId", required = false) String proveedorId,
            @RequestParam(value = "productoId", required = false) String productoId,
            Model model) {
        Double costo = 0.0;
        if (proveedorId != null && !proveedorId.isBlank() && productoId != null && !productoId.isBlank()) {
            Double ultimoCosto = compraProveedorService.obtenerUltimoCostoUnitario(proveedorId.trim(), productoId.trim());
            if (ultimoCosto != null) {
                costo = ultimoCosto;
            }
        }
        model.addAttribute("costo", costo);
        return "admin/fragments/costo-unitario :: bloqueCosto";
    }

    ///baja logica
    @PostMapping("/admin/purchase-orders/eliminar/{id}")
    public String eliminarCompra(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        try {
            compraProveedorService.eliminarCompraProveedor(id);
            return "redirect:/admin/orders?success=deleted";
        } catch (Exception e) {
            return "redirect:/admin/orders?error=" + e.getMessage();
        }
    }

    ///marcar como entregada la factura, que es lo que aumenta el stock
    @PostMapping({"/admin/compras/{id}/entregar", "/admin/purchase-orders/{id}/entregar", "/admin/compras/{id}/pagar", "/admin/purchase-orders/{id}/pagar"})
    public String marcarComoEntregada(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        try {
            compraProveedorService.marcarComoEntregada(id);
            redirectAttributes.addFlashAttribute("successMessage", "Mercadería recibida y stock actualizado");
            return "redirect:/admin/orders";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/orders";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar la entrega: " + e.getMessage());
            return "redirect:/admin/orders";
        }
    }

    //metodo que carga los datos para elegir en el formulario de registro de factura. se hace otro metodo porque
    //se usa en show y en registrarCompra (en caso de que falle, vuelve a mostrar lo mismo)
    private void cargarDatosModelo(Model model) {
        List<Proveedor> proveedores = proveedorService.listarActivos();
        if (proveedores == null || proveedores.isEmpty()) {
            proveedores = proveedorService.listarTodos();
        }

        List<Producto> productos = productoService.listarActivos();
        if (productos == null || productos.isEmpty()) {
            productos = productoService.listarTodos();
        }

        List<com.example.zero.dto.producto.ProductoDTO> prodDtos = new ArrayList<>();
        if (productos != null) {
            for (Producto p : productos) {
                double precio = 0.0;
                try {
                    precio = productoService.obtenerPrecioActual(p.getId());
                } catch (Exception ignored) {
                }
                prodDtos.add(com.example.zero.dto.producto.ProductoDTO.de(p, precio,0,null, productoService));
            }
        }
        List<FacturaProveedor> compras;
        try {
            compras = compraProveedorService.listarComprasProveedor();
        } catch (Exception e) {
            compras = java.util.Collections.emptyList();
        }

        model.addAttribute("proveedores", proveedores != null ? proveedores : java.util.Collections.emptyList());
        model.addAttribute("productos", prodDtos);
        model.addAttribute("formasDePago", TipoDePago.values());
        model.addAttribute("estadosFactura", EstadoFactura.values());
        model.addAttribute("compras", compras != null ? compras : java.util.Collections.emptyList());
    }
}
