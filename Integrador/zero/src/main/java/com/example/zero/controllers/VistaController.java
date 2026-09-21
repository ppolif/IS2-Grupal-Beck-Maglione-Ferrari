package com.example.zero.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VistaController {

    // Inicio / Portada
    @GetMapping({"/", "/shop", "/shop/index"})
    public String shopIndex() {
        return "shop/index";
    }

    // Catálogo de Productos / Categorías
    @GetMapping({"/shop/category", "/shop/categoria", "/shop/catalogo"})
    public String shopCategory() {
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
    @GetMapping({"/shop/single-product", "/shop/producto"})
    public String shopSingleProduct() {
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
<<<<<<< Updated upstream

    @GetMapping("/admin/register")
    public String showRegisterPage() {
        return "admin/page-register"; // o el nombre y ubicación de tu vista html
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
=======
>>>>>>> Stashed changes
}
