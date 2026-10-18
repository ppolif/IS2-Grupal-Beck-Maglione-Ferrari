package com.example.carrito.controladores;

import com.example.carrito.dtos.DatosPagoDTO;
import com.example.carrito.entidades.Carrito;
import com.example.carrito.entidades.Detalle;
import com.example.carrito.entidades.FacturaCompra;
import com.example.carrito.entidades.Usuario;
import com.example.carrito.excepciones.ErrorServicio;
import com.example.carrito.servicios.CarritoServicio;
import com.example.carrito.servicios.UsuarioServicio;
import com.example.carrito.servicios.compra.ProcesadorCompraFactory;
import com.example.carrito.servicios.compra.template.ProcesadorCompraTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/carrito")
public class CarritoControlador {

    @Autowired
    private CarritoServicio carritoServicio;

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private ProcesadorCompraFactory procesadorCompraFactory;

    @GetMapping
    public String verCarrito(Model model, Principal principal) {
        try {
            Usuario usuario = usuarioServicio.buscarPorEmail(principal.getName());
            Carrito carrito = carritoServicio.obtenerOCrearCarritoActivo(usuario);
            carritoServicio.recalcularTotal(carrito);

            List<Detalle> detallesActivos = carrito.getDetalles().stream()
                    .filter(d -> !d.isEliminado())
                    .collect(Collectors.toList());

            model.addAttribute("carrito", carrito);
            model.addAttribute("detalles", detallesActivos);
            model.addAttribute("usuarioLogueado", usuario);
            
            int totalItems = detallesActivos.stream().mapToInt(Detalle::getCantidad).sum();
            model.addAttribute("cantidadItemsCarrito", totalItems);

            return "carrito/index";
        } catch (ErrorServicio e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/catalogo";
        }
    }

    @PostMapping("/comprar")
    public String comprarDirecto(Principal principal, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = usuarioServicio.buscarPorEmail(principal.getName());
            Carrito carrito = carritoServicio.obtenerOCrearCarritoActivo(usuario);

            ProcesadorCompraTemplate procesador = procesadorCompraFactory.obtenerProcesador("DIRECTA");
            DatosPagoDTO datosPago = DatosPagoDTO.builder()
                    .metodoPago("DIRECTA")
                    .build();

            FacturaCompra orden = procesador.procesarCompra(carrito.getId(), usuario.getId(), datosPago);

            return "redirect:/checkout/exito/" + orden.getId();
        } catch (ErrorServicio e) {
            redirectAttributes.addFlashAttribute("error", "Error al procesar la compra: " + e.getMessage());
            return "redirect:/carrito";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ocurrió un error inesperado al procesar la compra.");
            return "redirect:/carrito";
        }
    }

    @PostMapping("/agregar")
    public String agregarItem(
            @RequestParam String articuloId,
            @RequestParam(defaultValue = "1") int cantidad,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Usuario usuario = usuarioServicio.buscarPorEmail(principal.getName());
            Carrito carrito = carritoServicio.obtenerOCrearCarritoActivo(usuario);
            carritoServicio.agregarArticulo(carrito.getId(), articuloId, cantidad);
            redirectAttributes.addFlashAttribute("exito", "¡Artículo agregado al carrito exitosamente!");
        } catch (ErrorServicio e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/carrito";
    }

    @PostMapping("/modificar")
    public String modificarCantidad(
            @RequestParam String detalleId,
            @RequestParam int cantidad,
            RedirectAttributes redirectAttributes
    ) {
        try {
            carritoServicio.modificarCantidad(detalleId, cantidad);
            redirectAttributes.addFlashAttribute("exito", "Cantidad actualizada.");
        } catch (ErrorServicio e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/carrito";
    }

    @PostMapping("/eliminar")
    public String eliminarItem(
            @RequestParam String detalleId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            carritoServicio.removerDetalle(detalleId);
            redirectAttributes.addFlashAttribute("exito", "Artículo removido del carrito.");
        } catch (ErrorServicio e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/carrito";
    }
}
