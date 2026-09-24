package com.example.zero.controllers;

import com.example.zero.entidades.producto.Producto;
import com.example.zero.entidades.producto.SubCategoria;
import com.example.zero.services.producto.ProductoService;
import com.example.zero.services.SubCategoriaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controlador para la gestión administrativa de productos (ABM).
 */
@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    private final ProductoService productoService;
    private final SubCategoriaService subCategoriaService;

    public AdminProductController(ProductoService productoService, SubCategoriaService subCategoriaService) {
        this.productoService = productoService;
        this.subCategoriaService = subCategoriaService;
    }

    /**
     * Listado de productos activos.
     */
    @GetMapping
    public String listProducts(Model model,
                               @RequestParam(name = "success", required = false) String success,
                               @RequestParam(name = "error", required = false) String error) {
        List<Producto> productos = productoService.listarActivos();
        for (Producto p : productos) {
            try {
                p.setPrecioActual(productoService.obtenerPrecioActual(p.getId()));
            } catch (Exception e) {
                p.setPrecioActual(0.0);
            }
        }

        model.addAttribute("products", productos);

        if ("created".equals(success)) {
            model.addAttribute("successMessage", "Producto creado exitosamente.");
        } else if ("updated".equals(success)) {
            model.addAttribute("successMessage", "Producto modificado exitosamente.");
        } else if ("deleted".equals(success)) {
            model.addAttribute("successMessage", "Producto dado de baja exitosamente.");
        }

        if (error != null) {
            model.addAttribute("errorMessage", error);
        }

        return "admin/products";
    }

    /**
     * Formulario para crear un nuevo producto.
     */
    @GetMapping("/nuevo")
    public String showCreateForm(Model model) {
        List<SubCategoria> subcategorias = subCategoriaService.listarActivas();
        model.addAttribute("subcategorias", subcategorias);
        model.addAttribute("isEdit", false);
        model.addAttribute("producto", new Producto());
        model.addAttribute("precioActual", 0.0);
        return "admin/product-form";
    }

    /**
     * Procesar la creación de un nuevo producto.
     */
    @PostMapping("/guardar")
    public String createProduct(@RequestParam("codigo") String codigo,
                                @RequestParam("nombre") String nombre,
                                @RequestParam(name = "descripcion", required = false) String descripcion,
                                @RequestParam(name = "talle", required = false) String talle,
                                @RequestParam("subCategoriaId") String subCategoriaId,
                                @RequestParam("precio") double precio,
                                @RequestParam(name = "enOferta", defaultValue = "false") boolean enOferta,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        try {
            productoService.crearProducto(codigo, nombre, descripcion, talle, subCategoriaId, precio, enOferta);
            return "redirect:/admin/products?success=created";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("subcategorias", subCategoriaService.listarActivas());
            model.addAttribute("isEdit", false);
            model.addAttribute("codigo", codigo);
            model.addAttribute("nombre", nombre);
            model.addAttribute("descripcion", descripcion);
            model.addAttribute("talle", talle);
            model.addAttribute("selectedSubcategoriaId", subCategoriaId);
            model.addAttribute("precio", precio);
            model.addAttribute("enOferta", enOferta);
            return "admin/product-form";
        }
    }

    /**
     * Formulario para editar un producto existente.
     */
    @GetMapping("/editar/{id}")
    public String showEditForm(@PathVariable("id") String id, Model model) {
        try {
            Producto producto = productoService.buscarPorId(id);
            double precioActual = 0.0;
            try {
                precioActual = productoService.obtenerPrecioActual(id);
            } catch (Exception ignored) {
            }
            producto.setPrecioActual(precioActual);

            model.addAttribute("producto", producto);
            model.addAttribute("precioActual", precioActual);
            model.addAttribute("subcategorias", subCategoriaService.listarActivas());
            model.addAttribute("isEdit", true);
            return "admin/product-form";
        } catch (IllegalArgumentException e) {
            return "redirect:/admin/products?error=" + e.getMessage();
        }
    }

    /**
     * Procesar la modificación de un producto existente.
     */
    @PostMapping("/editar/{id}")
    public String updateProduct(@PathVariable("id") String id,
                                @RequestParam("nombre") String nombre,
                                @RequestParam(name = "descripcion", required = false) String descripcion,
                                @RequestParam(name = "talle", required = false) String talle,
                                @RequestParam("subCategoriaId") String subCategoriaId,
                                @RequestParam(name = "precio", required = false) Double precio,
                                @RequestParam(name = "enOferta", defaultValue = "false") boolean enOferta,
                                Model model) {
        try {
            productoService.modificarProducto(id, nombre, descripcion, talle, subCategoriaId, enOferta);

            if (precio != null && precio > 0) {
                double precioActual = 0.0;
                try {
                    precioActual = productoService.obtenerPrecioActual(id);
                } catch (Exception ignored) {
                }
                if (Math.abs(precioActual - precio) > 0.001) {
                    productoService.actualizarPrecio(id, precio);
                }
            }

            return "redirect:/admin/products?success=updated";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("subcategorias", subCategoriaService.listarActivas());
            model.addAttribute("isEdit", true);
            try {
                Producto prod = productoService.buscarPorId(id);
                prod.setNombre(nombre);
                prod.setDescripcion(descripcion);
                prod.setTalle(talle);
                prod.setEnOferta(enOferta);
                model.addAttribute("producto", prod);
            } catch (Exception ignored) {
            }
            model.addAttribute("precioActual", precio != null ? precio : 0.0);
            return "admin/product-form";
        }
    }

    /**
     * Baja lógica de un producto (POST).
     */
    @PostMapping("/eliminar/{id}")
    public String deleteProductPost(@PathVariable("id") String id) {
        productoService.eliminarProducto(id);
        return "redirect:/admin/products?success=deleted";
    }

    /**
     * Baja lógica de un producto (GET para facilidad desde links).
     */
    @GetMapping("/eliminar/{id}")
    public String deleteProductGet(@PathVariable("id") String id) {
        productoService.eliminarProducto(id);
        return "redirect:/admin/products?success=deleted";
    }
}

