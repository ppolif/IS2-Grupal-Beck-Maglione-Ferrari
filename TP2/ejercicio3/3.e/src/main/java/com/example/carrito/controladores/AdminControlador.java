package com.example.carrito.controladores;

import com.example.carrito.entidades.Proveedor;
import com.example.carrito.excepciones.ErrorServicio;
import com.example.carrito.servicios.ArticuloServicio;
import com.example.carrito.servicios.ProveedorServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminControlador {

    @Autowired
    private ProveedorServicio proveedorServicio;

    @Autowired
    private ArticuloServicio articuloServicio;

    // =========================================================================
    // HU6: ALTA DE PROVEEDOR
    // =========================================================================

    @GetMapping("/proveedores/nuevo")
    public String formularioProveedor(Model model) {
        model.addAttribute("proveedores", proveedorServicio.listarActivos());
        return "admin/proveedor-form";
    }

    @PostMapping("/proveedores/nuevo")
    public String registrarProveedor(
            @RequestParam String nombre,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        try {
            Proveedor guardado = proveedorServicio.registrarProveedor(nombre);
            redirectAttributes.addFlashAttribute("exito",
                    "Proveedor '" + guardado.getNombre() + "' registrado correctamente. Disponible inmediatamente.");
            return "redirect:/admin/proveedores/nuevo";
        } catch (ErrorServicio e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("proveedores", proveedorServicio.listarActivos());
            return "admin/proveedor-form";
        }
    }

    // =========================================================================
    // HU7: ALTA DE PRODUCTO / ARTÍCULO
    // =========================================================================

    @GetMapping("/articulos/nuevo")
    public String formularioArticulo(Model model) {
        List<Proveedor> proveedores = proveedorServicio.listarActivos();
        model.addAttribute("proveedores", proveedores);
        return "admin/producto-form";
    }

    @PostMapping("/articulos/nuevo")
    public String registrarArticulo(
            @RequestParam String nombre,
            @RequestParam double precio,
            @RequestParam String proveedorId,
            @RequestParam(required = false) MultipartFile archivoImagen,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        try {
            articuloServicio.registrarArticulo(nombre, precio, proveedorId, archivoImagen);
            redirectAttributes.addFlashAttribute("exito",
                    "¡Artículo registrado con éxito y publicado en el catálogo!");
            return "redirect:/catalogo";
        } catch (ErrorServicio e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("proveedores", proveedorServicio.listarActivos());
            return "admin/producto-form";
        }
    }
}

