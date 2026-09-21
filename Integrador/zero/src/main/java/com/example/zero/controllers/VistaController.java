package com.example.zero.controllers;

import com.example.zero.entidades.producto.Categoria;
import com.example.zero.entidades.producto.Producto;
import com.example.zero.services.CategoriaService;
import com.example.zero.services.ProductoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class VistaController {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;

    public VistaController(ProductoService productoService, CategoriaService categoriaService) {
        this.productoService = productoService;
        this.categoriaService = categoriaService;
    }

    // Inicio / Portada
    @GetMapping({"/", "/shop", "/shop/index"})
    public String shopIndex(Model model) {
        List<Producto> productos = productoService.listarActivos();
        for (Producto p : productos) {
            try {
                p.setPrecioActual(productoService.obtenerPrecioActual(p.getId()));
            } catch (Exception e) {
                p.setPrecioActual(0.0);
            }
        }
        model.addAttribute("featuredProducts", productos);
        return "shop/index";
    }

    // Catálogo de Productos / Categorías
    @GetMapping({"/shop/category", "/shop/categoria", "/shop/catalogo"})
    public String shopCategory(Model model,
                               @RequestParam(name = "categoryId", required = false) String categoryId,
                               @RequestParam(name = "maxPrice", required = false) Double maxPrice) {
        List<Categoria> categorias = categoriaService.listarActivas();
        List<Producto> productos = productoService.listarActivos();

        if (categoryId != null && !categoryId.trim().isEmpty()) {
            productos = productos.stream()
                    .filter(p -> p.getSubCategoria() != null &&
                            p.getSubCategoria().getCategoria() != null &&
                            categoryId.equals(p.getSubCategoria().getCategoria().getId()))
                    .toList();
            model.addAttribute("selectedCategory", categoryId);
        }

        for (Producto p : productos) {
            try {
                p.setPrecioActual(productoService.obtenerPrecioActual(p.getId()));
            } catch (Exception e) {
                p.setPrecioActual(0.0);
            }
        }

        if (maxPrice != null && maxPrice > 0) {
            productos = productos.stream()
                    .filter(p -> p.getPrecioActual() <= maxPrice)
                    .toList();
        }

        model.addAttribute("categories", categorias);
        model.addAttribute("products", productos);
        model.addAttribute("totalProducts", productos.size());
        return "shop/category";
    }

    // Carrito de compras
    @GetMapping({"/shop/cart", "/shop/carrito"})
    public String shopCart() {
        return "shop/cart";
    }

    // Finalizar compra / Checkout
    @GetMapping({"/shop/checkout", "/shop/pagar"})
    public String shopCheckout() {
        return "shop/checkout";
    }

    // Confirmación de pedido
    @GetMapping({"/shop/confirmation", "/shop/confirmacion"})
    public String shopConfirmation() {
        return "shop/confirmation";
    }

    // Ficha de producto individual
    @GetMapping({"/shop/single-product", "/shop/producto", "/shop/product/{id}"})
    public String shopSingleProduct(Model model,
                                    @PathVariable(name = "id", required = false) String pathId,
                                    @RequestParam(name = "id", required = false) String paramId) {
        String id = pathId != null ? pathId : paramId;
        if (id != null) {
            try {
                Producto prod = productoService.buscarPorId(id);
                try {
                    prod.setPrecioActual(productoService.obtenerPrecioActual(id));
                } catch (Exception ignored) {
                }
                model.addAttribute("product", prod);
            } catch (Exception ignored) {
            }
        }
        return "shop/single-product";
    }

    // Dashboard
    @GetMapping({"/admin", "/admin/index"})
    public String adminIndex() {
        return "admin/index";
    }

    // 1. Tablas básicas (orders / tables-basic)
    @GetMapping({"/admin/orders", "/admin/tables-basic"})
    public String adminTables() {
        return "admin/tables-basic";
    }

    // 2. Pantalla 404
    @GetMapping({"/admin/404", "/admin/page-404"})
    public String admin404() {
        return "admin/page-404";
    }

    @GetMapping({"/admin/ventas/nueva", "/admin/registrar-venta"})
    public String adminRegistrarVenta() {
        return "admin/registrar-venta";
    }

    // 2. Registrar Compra (Ingreso de mercadería con proveedores y stock)
    @GetMapping({"/admin/compras/nueva", "/admin/registrar-compra"})
    public String adminRegistrarCompra() {
        return "admin/registrar-compra";
    }
}
