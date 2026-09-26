package com.example.zero.controllers;

import com.example.zero.entidades.producto.Producto;
import com.example.zero.entidades.producto.SubCategoria;
import com.example.zero.services.producto.ProductoService;
import com.example.zero.services.SubCategoriaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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
        model.addAttribute("stock", 0);
        model.addAttribute("codigo", "");
        model.addAttribute("nombre", "");
        model.addAttribute("descripcion", "");
        model.addAttribute("talle", "");
        model.addAttribute("selectedSubcategoriaId", "");
        model.addAttribute("enOferta", false);
        return "admin/product-form";
    }

    /**
     * Procesar la creación de un nuevo producto con imagen obligatoria y stock.
     */
    @PostMapping("/guardar")
    public String createProduct(@RequestParam("codigo") String codigo,
                                @RequestParam("nombre") String nombre,
                                @RequestParam(name = "descripcion", required = false) String descripcion,
                                @RequestParam(name = "talle", required = false) String talle,
                                @RequestParam("subCategoriaId") String subCategoriaId,
                                @RequestParam("precio") double precio,
                                @RequestParam(name = "stock", defaultValue = "0") int stock,
                                @RequestParam(name = "enOferta", defaultValue = "false") boolean enOferta,
                                @RequestParam(name = "imagen", required = false) MultipartFile imagen,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (imagen == null || imagen.isEmpty()) {
            model.addAttribute("errorMessage", "Debe seleccionar obligatoriamente una imagen del producto");
            model.addAttribute("subcategorias", subCategoriaService.listarActivas());
            model.addAttribute("isEdit", false);
            model.addAttribute("codigo", codigo);
            model.addAttribute("nombre", nombre);
            model.addAttribute("descripcion", descripcion);
            model.addAttribute("talle", talle);
            model.addAttribute("selectedSubcategoriaId", subCategoriaId);
            model.addAttribute("precio", precio);
            model.addAttribute("stock", stock);
            model.addAttribute("enOferta", enOferta);
            return "admin/product-form";
        }

        try {
            if (stock > 0) {
                productoService.crearProducto(codigo, nombre, descripcion, talle, subCategoriaId, precio, enOferta, stock, imagen);
            } else {
                productoService.crearProducto(codigo, nombre, descripcion, talle, subCategoriaId, precio, enOferta, imagen);
            }
            return "redirect:/admin/products?success=created";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage() != null ? e.getMessage() : "Error al crear el producto");
            model.addAttribute("subcategorias", subCategoriaService.listarActivas());
            model.addAttribute("isEdit", false);
            model.addAttribute("codigo", codigo);
            model.addAttribute("nombre", nombre);
            model.addAttribute("descripcion", descripcion);
            model.addAttribute("talle", talle);
            model.addAttribute("selectedSubcategoriaId", subCategoriaId);
            model.addAttribute("precio", precio);
            model.addAttribute("stock", stock);
            model.addAttribute("enOferta", enOferta);
            return "admin/product-form";
        }
    }

    /**
     * Sobrecarga para compatibilidad con llamadas y tests existentes con imagen pero sin stock.
     */
    public String createProduct(String codigo, String nombre, String descripcion, String talle,
                                String subCategoriaId, double precio, boolean enOferta,
                                MultipartFile imagen, Model model, RedirectAttributes redirectAttributes) {
        return createProduct(codigo, nombre, descripcion, talle, subCategoriaId, precio, 0, enOferta, imagen, model, redirectAttributes);
    }

    /**
     * Sobrecarga para compatibilidad con llamadas y tests existentes sin MultipartFile.
     */
    public String createProduct(String codigo, String nombre, String descripcion, String talle,
                                String subCategoriaId, double precio, boolean enOferta,
                                Model model, RedirectAttributes redirectAttributes) {
        try {
            productoService.crearProducto(codigo, nombre, descripcion, talle, subCategoriaId, precio, enOferta);
            return "redirect:/admin/products?success=created";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage() != null ? e.getMessage() : "Error al crear el producto");
            model.addAttribute("subcategorias", subCategoriaService.listarActivas());
            model.addAttribute("isEdit", false);
            model.addAttribute("codigo", codigo);
            model.addAttribute("nombre", nombre);
            model.addAttribute("descripcion", descripcion);
            model.addAttribute("talle", talle);
            model.addAttribute("selectedSubcategoriaId", subCategoriaId);
            model.addAttribute("precio", precio);
            model.addAttribute("stock", 0);
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
            model.addAttribute("stock", producto.getStock());
            model.addAttribute("subcategorias", subCategoriaService.listarActivas());
            model.addAttribute("isEdit", true);
            return "admin/product-form";
        } catch (Exception e) {
            return "redirect:/admin/products?error=" + (e.getMessage() != null ? e.getMessage() : "Error al cargar el producto");
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
                                @RequestParam(name = "stock", required = false) Integer stock,
                                @RequestParam(name = "enOferta", defaultValue = "false") boolean enOferta,
                                Model model) {
        try {
            if (stock != null) {
                productoService.modificarProducto(id, nombre, descripcion, talle, subCategoriaId, enOferta, stock);
            } else {
                productoService.modificarProducto(id, nombre, descripcion, talle, subCategoriaId, enOferta);
            }

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
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage() != null ? e.getMessage() : "Error al modificar el producto");
            model.addAttribute("subcategorias", subCategoriaService.listarActivas());
            model.addAttribute("isEdit", true);
            try {
                Producto prod = productoService.buscarPorId(id);
                prod.setNombre(nombre);
                prod.setDescripcion(descripcion);
                prod.setTalle(talle);
                prod.setEnOferta(enOferta);
                if (stock != null) {
                    prod.setStock(stock);
                }
                model.addAttribute("producto", prod);
            } catch (Exception ignored) {
            }
            model.addAttribute("precioActual", precio != null ? precio : 0.0);
            model.addAttribute("stock", stock != null ? stock : 0);
            return "admin/product-form";
        }
    }

    /**
     * Sobrecarga para tests existentes de modificación sin stock.
     */
    public String updateProduct(String id, String nombre, String descripcion, String talle,
                                String subCategoriaId, Double precio, boolean enOferta, Model model) {
        return updateProduct(id, nombre, descripcion, talle, subCategoriaId, precio, null, enOferta, model);
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
