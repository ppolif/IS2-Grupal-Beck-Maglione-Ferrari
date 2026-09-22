package com.example.zero.controllers;

import com.example.zero.entidades.producto.Producto;
import com.example.zero.repositories.CategoriaRepository;
import com.example.zero.repositories.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class VistaController {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    // Inicio / Portada
    @GetMapping({"/", "/shop", "/shop/index"})
    public String shopIndex(Model model) {
        List<Producto> featured = productoRepository.findByEliminadoFalse();
        model.addAttribute("featuredProducts", featured);
        return "shop/index";
    }

    // Catálogo de Productos / Categorías
    @GetMapping({"/shop/category", "/shop/categoria", "/shop/catalogo"})
    public String shopCategory(@RequestParam(value = "categoryId", required = false) String categoryId,
                               @RequestParam(value = "maxPrice", required = false) Double maxPrice,
                               Model model) {
        List<Producto> products = productoRepository.findByEliminadoFalse();
        if (categoryId != null && !categoryId.trim().isEmpty()) {
            products = products.stream()
                    .filter(p -> p.getSubCategoria() != null && p.getSubCategoria().getCategoria() != null &&
                            categoryId.trim().equalsIgnoreCase(p.getSubCategoria().getCategoria().getId()))
                    .toList();
            model.addAttribute("selectedCategory", categoryId.trim());
        }
        if (maxPrice != null && maxPrice > 0) {
            products = products.stream()
                    .filter(p -> p.getPrecioActual() != null && p.getPrecioActual() <= maxPrice)
                    .toList();
        }

        model.addAttribute("products", products);
        model.addAttribute("totalProducts", products.size());
        model.addAttribute("categories", categoriaRepository.findByEliminadoFalse());
        return "shop/category";
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
    @GetMapping({"/shop/single-product", "/shop/producto"})
    public String shopSingleProduct() {
        return "shop/single-product";
    }

    // Dashboard Admin
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

    // Registrar Compra (Ingreso de mercadería con proveedores y stock)
    @GetMapping({"/admin/compras/nueva", "/admin/registrar-compra"})
    public String adminRegistrarCompra() {
        return "admin/registrar-compra";
    }
}
