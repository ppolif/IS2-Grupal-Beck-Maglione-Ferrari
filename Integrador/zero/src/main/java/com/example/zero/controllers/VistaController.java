package com.example.zero.controllers;

import com.example.zero.entidades.compra.Factura;
import com.example.zero.entidades.persona.Cliente;
import com.example.zero.entidades.persona.Usuario;
import com.example.zero.entidades.producto.Producto;
import com.example.zero.enums.RolUsuario;
import com.example.zero.services.CategoriaService;
import com.example.zero.services.VentaService;
import com.example.zero.services.producto.ProductoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class VistaController {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;
    private final VentaService ventaService;
    private final AdminVentaController adminVentaController;
    private final com.example.zero.repositories.SubCategoriaRepository subCategoriaRepository;
    private final com.example.zero.services.OrdenCompraService ordenCompraService;
    private final com.example.zero.services.VigenciaPrecioService vigenciaPrecioService;
    private final com.example.zero.services.StockService stockService;
    private final com.example.zero.services.ContactoService contactoService;

    // Inicio / Portada
    @GetMapping({"/", "/shop", "/shop/index"})
    public String shopIndex(Model model) {
        List<com.example.zero.dto.producto.ProductoDTO> featured = productoService.listarActivos().stream()
                .map(p -> com.example.zero.dto.producto.ProductoDTO.de(p, vigenciaPrecioService, stockService, productoService))
                .toList();
        model.addAttribute("featuredProducts", featured);

        List<com.example.zero.dto.producto.ProductoDTO> sale = productoService.listarEnOferta().stream()
                .map(p -> com.example.zero.dto.producto.ProductoDTO.de(p, vigenciaPrecioService, stockService, productoService))
                .toList();
        model.addAttribute("saleProducts", sale);

        return "shop/index";
    }

    // Catálogo de Productos / Categorías
    @GetMapping({"/shop/category", "/shop/categoria", "/shop/catalogo"})
    public String shopCategory(@RequestParam(value = "categoryId", required = false) String categoryId,
                               @RequestParam(value = "subCategoryId", required = false) String subCategoryId,
                               @RequestParam(value = "maxPrice", required = false) Double maxPrice,
                               Model model) {
        List<com.example.zero.dto.producto.ProductoDTO> products = productoService.listarActivos().stream()
                .map(p -> com.example.zero.dto.producto.ProductoDTO.de(p, vigenciaPrecioService, stockService, productoService))
                .toList();

        if (subCategoryId != null && !subCategoryId.trim().isEmpty()) {
            products = products.stream()
                    .filter(p -> p.getSubCategoria() != null &&
                            subCategoryId.trim().equalsIgnoreCase(p.getSubCategoria().getId()))
                    .toList();
            model.addAttribute("selectedSubCategory", subCategoryId.trim());

            if (categoryId == null || categoryId.trim().isEmpty()) {
                subCategoriaRepository.findById(subCategoryId.trim()).ifPresent(sc -> {
                    if (sc.getCategoria() != null) {
                        model.addAttribute("selectedCategory", sc.getCategoria().getId());
                    }
                });
            } else {
                model.addAttribute("selectedCategory", categoryId.trim());
            }
        } else if (categoryId != null && !categoryId.trim().isEmpty()) {
            products = products.stream()
                    .filter(p -> p.getSubCategoria() != null && p.getSubCategoria().getCategoria() != null &&
                            categoryId.trim().equalsIgnoreCase(p.getSubCategoria().getCategoria().getId()))
                    .toList();
            model.addAttribute("selectedCategory", categoryId.trim());
        }

        if (maxPrice != null && maxPrice > 0) {
            products = products.stream()
                    .filter(p -> p.getPrecioActual() <= maxPrice)
                    .toList();
            model.addAttribute("maxPrice", maxPrice);
        }

        model.addAttribute("products", products);
        model.addAttribute("totalProducts", products.size());
        model.addAttribute("categories", categoriaService.listarConSubcategorias());
        return "shop/category";
    }

    // Catálogo de Ofertas
    @GetMapping({"/offers", "/ofertas", "/shop/offers", "/shop/ofertas"})
    public String shopOffers(Model model) {
        List<com.example.zero.dto.producto.ProductoDTO> offers = productoService.listarEnOferta().stream()
                .map(p -> com.example.zero.dto.producto.ProductoDTO.de(p, vigenciaPrecioService, stockService, productoService))
                .toList();
        model.addAttribute("products", offers);
        model.addAttribute("totalProducts", offers.size());
        model.addAttribute("categories", categoriaService.listarConSubcategorias());
        model.addAttribute("isOffersPage", true);
        model.addAttribute("title", "Ofertas Especiales");
        return "shop/category";
    }

    // Finalizar compra / Checkout
    @GetMapping({"/shop/checkout", "/shop/pagar"})
    public String shopCheckout(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuariosession");
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Debes iniciar sesión para acceder al proceso de compra.");
            return "redirect:/login";
        }
        if (usuario.getRol() != RolUsuario.CLIENTE) {
            redirectAttributes.addFlashAttribute("errorMessage", "Los usuarios administradores no pueden acceder al proceso de compra.");
            return "redirect:/admin/registrar-venta";
        }

        try {
            com.example.zero.entidades.persona.Cliente cliente = ordenCompraService.obtenerOAsociarCliente(usuario);
            session.setAttribute("usuariosession", usuario);

            com.example.zero.entidades.compraCliente.OrdenCompra carrito = ordenCompraService.obtenerOCrearCarrito(cliente);
            List<com.example.zero.entidades.compraCliente.DetalleCompra> items = ordenCompraService.obtenerItemsActivos(carrito);

            model.addAttribute("cliente", cliente);
            model.addAttribute("usuario", usuario);
            model.addAttribute("cart", carrito);
            model.addAttribute("items", items);

            String telefono = "";
            if (contactoService != null) {
                telefono = contactoService.obtenerTelefonoPrincipal(cliente).orElse("");
            }
            model.addAttribute("customerPhone", telefono);

            String direccion = "";
            String ciudad = "";
            String zipCode = "";
            if (cliente.getDireccion() != null && !cliente.getDireccion().isEmpty()) {
                com.example.zero.entidades.zona.Direccion dir = cliente.getDireccion().get(0);
                String calle = dir.getCalle() != null ? dir.getCalle() : "";
                String num = dir.getNumeracion() != null ? dir.getNumeracion() : "";
                direccion = (calle + " " + num).trim();
                if (dir.getLocalidad() != null) {
                    ciudad = dir.getLocalidad().getNombre() != null ? dir.getLocalidad().getNombre() : "";
                    zipCode = dir.getLocalidad().getCodigoPostal() != null ? dir.getLocalidad().getCodigoPostal() : "";
                }
            }
            model.addAttribute("customerAddress", direccion);
            model.addAttribute("customerCity", ciudad);
            model.addAttribute("customerZip", zipCode);

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar datos del checkout: " + e.getMessage());
        }

        return "shop/checkout";
    }

    // Confirmación de pedido
    @GetMapping({"/shop/confirmation", "/shop/confirmacion"})
    public String shopConfirmation(HttpSession session,
                                   Model model,
                                   @RequestParam(name = "orderNumber", required = false) String orderNumber) {
        Usuario usuario = (Usuario) session.getAttribute("usuariosession");
        if (usuario != null && usuario.getRol() != RolUsuario.CLIENTE) {
            if (orderNumber != null && !orderNumber.trim().isEmpty()) {
                return "redirect:/admin/orders/" + orderNumber.trim();
            }
            return "redirect:/admin/ventas";
        }

        model.addAttribute("title", "Confirmación de Pedido");
        model.addAttribute("subtitle", "Comprobante");
        if (orderNumber != null && !orderNumber.trim().isEmpty()) {
            try {
                Factura factura = ventaService.buscarFacturaPorIdentificador(orderNumber);
                if (factura != null) {
                    model.addAttribute("factura", factura);
                    model.addAttribute("order", factura);

                    Cliente cliente = ventaService.obtenerClienteDeFactura(factura);
                    String customerName = ventaService.obtenerNombreComprobante(factura);
                    String customerEmail = ventaService.obtenerEmailComprobante(factura);
                    String customerPhone = "";
                    String shippingAddress = "";
                    String shippingCity = "";
                    String shippingZip = "";

                    if (cliente != null) {
                        if (contactoService != null) {
                            customerPhone = contactoService.obtenerTelefonoPrincipal(cliente).orElse("");
                        }
                        if (cliente.getDireccion() != null && !cliente.getDireccion().isEmpty()) {
                            for (var dir : cliente.getDireccion()) {
                                if (dir != null && !dir.isEliminado() && dir.getCalle() != null && !dir.getCalle().isBlank()) {
                                    String calle = dir.getCalle().trim();
                                    String num = dir.getNumeracion() != null ? dir.getNumeracion().trim() : "";
                                    shippingAddress = (calle + " " + num).trim();
                                    if (dir.getLocalidad() != null) {
                                        shippingCity = dir.getLocalidad().getNombre() != null ? dir.getLocalidad().getNombre().trim() : "";
                                        shippingZip = dir.getLocalidad().getCodigoPostal() != null ? dir.getLocalidad().getCodigoPostal().trim() : "";
                                    }
                                    break;
                                }
                            }
                        }
                    }

                    model.addAttribute("cliente", cliente);
                    model.addAttribute("customerName", customerName);
                    model.addAttribute("customerEmail", customerEmail);
                    model.addAttribute("customerPhone", customerPhone);
                    model.addAttribute("shippingAddress", shippingAddress);
                    model.addAttribute("shippingCity", shippingCity);
                    model.addAttribute("shippingZip", shippingZip);
                }
            } catch (Exception ignored) {
            }
        }
        return "shop/confirmation";
    }

    // Ficha de producto individual
    @GetMapping({"/products/{id}", "/product/{id}", "/shop/product/{id}", "/shop/products/{id}", "/shop/single-product", "/shop/producto"})
    public String shopSingleProduct(@PathVariable(value = "id", required = false) String pathId,
                                    @RequestParam(value = "id", required = false) String paramId,
                                    jakarta.servlet.http.HttpServletResponse response,
                                    Model model) {
        String id = pathId != null ? pathId : paramId;
        if (id == null || id.trim().isEmpty()) {
            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND);
            model.addAttribute("errorMessage", "No se especificó un identificador de producto válido.");
            return "admin/page-404";
        }

        Producto p = null;
        try {
            p = productoService.buscarPorId(id.trim());
        } catch (Exception e1) {
            try {
                p = productoService.buscarPorCodigo(id.trim());
            } catch (Exception ignored) {}
        }

        if (p == null || p.isEliminado()) {
            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND);
            model.addAttribute("errorMessage", "El producto solicitado no existe o fue dado de baja del catálogo.");
            return "admin/page-404";
        }

        com.example.zero.dto.producto.ProductoDTO dto = com.example.zero.dto.producto.ProductoDTO.de(p, vigenciaPrecioService, stockService, productoService);
        model.addAttribute("product", dto);
        model.addAttribute("title", "Detalle del Producto");
        model.addAttribute("subtitle", p.getNombre());
        model.addAttribute("categoryName", dto.getCategoryName());
        model.addAttribute("stock", dto.getStock());
        model.addAttribute("imageUrl", dto.getImageUrl());
        return "shop/single-product";
    }

    // Panel Admin redirige directamente a Registrar Venta
    @GetMapping({"/admin", "/admin/", "/admin/index", "/admin/index.html", "/admin/dashboard"})
    public String adminIndex() {
        return "redirect:/admin/registrar-venta";
    }

    // Pantalla 404
    @GetMapping({"/admin/404", "/admin/page-404"})
    public String admin404() {
        return "admin/page-404";
    }
}
