package com.example.zero.controllers;

import com.example.zero.entidades.compra.Factura;
import com.example.zero.entidades.persona.Cliente;
import com.example.zero.entidades.persona.Usuario;
import com.example.zero.enums.RolUsuario;
import com.example.zero.services.OrdenCompraService;
import com.example.zero.services.VentaService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

///controlador para el historial de compraas en el perfil de un usuario
@Controller
@RequiredArgsConstructor
public class HistorialController {

    private final VentaService ventaService;
    private final OrdenCompraService ordenCompraService;

    ///mostrar el historial
    @GetMapping({"/historial", "/shop/historial"})
    public String verHistorial(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuariosession");
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Debes iniciar sesión para consultar tu historial de compras.");
            return "redirect:/login";
        }

        if (usuario.getRol() != RolUsuario.CLIENTE) {
            redirectAttributes.addFlashAttribute("infoMessage", "Los usuarios administradores gestionan las ventas desde el panel administrativo.");
            return "redirect:/admin/ventas";
        }

        try {
            Cliente cliente = ordenCompraService.obtenerOAsociarCliente(usuario);
            session.setAttribute("usuariosession", usuario);

            List<Factura> compras = ventaService.listarComprasCliente(cliente);
            double totalGastado = compras.stream().mapToDouble(Factura::getTotalPagado).sum();

            model.addAttribute("title", "Mi Historial de Compras");
            model.addAttribute("subtitle", "Historial");
            model.addAttribute("cliente", cliente);
            model.addAttribute("compras", compras);
            model.addAttribute("totalCompras", compras.size());
            model.addAttribute("totalGastado", totalGastado);

            return "shop/historial";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar el historial de compras: " + e.getMessage());
            model.addAttribute("title", "Mi Historial de Compras");
            model.addAttribute("subtitle", "Historial");
            return "shop/historial";
        }
    }

    ///Redirige al comprobante y detalle de una compra específica.
    @GetMapping({"/historial/{orderNumber}", "/shop/historial/{orderNumber}"})
    public String verDetalleCompra(@PathVariable("orderNumber") String orderNumber) {
        return "redirect:/shop/confirmation?orderNumber=" + orderNumber;
    }
}
