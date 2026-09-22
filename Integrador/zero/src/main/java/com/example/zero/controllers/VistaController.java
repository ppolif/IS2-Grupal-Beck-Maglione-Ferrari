package com.example.zero.controllers;

import com.example.zero.dto.OrderViewDto;
import com.example.zero.entidades.compra.Factura;
import com.example.zero.entidades.producto.Producto;
import com.example.zero.repositories.CategoriaRepository;
import com.example.zero.repositories.ProductoRepository;
import com.example.zero.services.VentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class VistaController {

    private final AdminVentaController adminVentaController;
    private final VentaService ventaService;
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
    public String shopConfirmation(Model model,
                                   @RequestParam(name = "orderNumber", required = false) String orderNumber) {
        if (orderNumber != null && !orderNumber.trim().isEmpty()) {
            try {
                String cleanNum = orderNumber.replace("#ORD-", "").replace("ORD-", "").trim();
                Long num = Long.parseLong(cleanNum);
                Factura f = ventaService.buscarPorNumeroFactura(num);
                OrderViewDto dto = adminVentaController.mapearFacturaAOrderDto(f);
                model.addAttribute("order", dto);
            } catch (Exception ignored) {
            }
        }
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

    // Pantalla 404
    @GetMapping({"/admin/404", "/admin/page-404"})
    public String admin404() {
        return "admin/page-404";
    }

    // Registrar Compra (Ingreso de mercadería con proveedores y stock)
    @GetMapping({"/admin/compras/nueva", "/admin/registrar-compra"})
    public String adminRegistrarCompra() {
        return "admin/registrar-compra";
    }
}
