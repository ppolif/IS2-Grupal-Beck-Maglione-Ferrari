package com.example.carrito.controladores;

import com.example.carrito.dtos.DatosPagoDTO;
import com.example.carrito.entidades.Carrito;
import com.example.carrito.entidades.Detalle;
import com.example.carrito.entidades.RegistroCompra;
import com.example.carrito.entidades.Usuario;
import com.example.carrito.excepciones.ErrorServicio;
import com.example.carrito.repositorios.RegistroCompraRepositorio;
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

@Controller
public class CheckoutControlador {

    @Autowired
    private CarritoServicio carritoServicio;

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private ProcesadorCompraFactory procesadorCompraFactory;

    @Autowired
    private RegistroCompraRepositorio registroCompraRepositorio;

    @GetMapping("/checkout")
    public String mostrarCheckout() {
        return "redirect:/carrito";
    }

    @PostMapping("/checkout/procesar")
    public String procesarCompra(
            @ModelAttribute("datosPago") DatosPagoDTO datosPago,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Usuario usuario = usuarioServicio.buscarPorEmail(principal.getName());
            Carrito carrito = carritoServicio.obtenerOCrearCarritoActivo(usuario);

            String metodo = (datosPago != null && datosPago.getMetodoPago() != null && !datosPago.getMetodoPago().trim().isEmpty())
                    ? datosPago.getMetodoPago()
                    : "DIRECTA";

            ProcesadorCompraTemplate procesador = procesadorCompraFactory.obtenerProcesador(metodo);

            DatosPagoDTO datosEfectivos = datosPago != null ? datosPago : DatosPagoDTO.builder().metodoPago(metodo).build();
            RegistroCompra orden = procesador.procesarCompra(carrito.getId(), usuario.getId(), datosEfectivos);

            return "redirect:/checkout/exito/" + orden.getId();
        } catch (ErrorServicio e) {
            redirectAttributes.addFlashAttribute("error", "Error al procesar la compra: " + e.getMessage());
            return "redirect:/carrito";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ocurrió un error inesperado al procesar la compra.");
            return "redirect:/carrito";
        }
    }

    @GetMapping({"/checkout/exito/{ordenId}", "/compra/exito/{ordenId}"})
    public String mostrarExito(@PathVariable String ordenId, Model model, Principal principal) {
        RegistroCompra orden = registroCompraRepositorio.findById(ordenId)
                .orElse(null);

        if (orden == null) {
            return "redirect:/catalogo";
        }

        model.addAttribute("orden", orden);
        model.addAttribute("cantidadItemsCarrito", 0);
        return "checkout/exito";
    }

    @GetMapping("/historial")
    public String verHistorial(Model model, Principal principal) {
        try {
            Usuario usuario = usuarioServicio.buscarPorEmail(principal.getName());
            List<RegistroCompra> historial = registroCompraRepositorio
                    .findByUsuarioIdAndEliminadoFalseOrderByFechaDesc(usuario.getId());

            Carrito carrito = carritoServicio.obtenerOCrearCarritoActivo(usuario);
            int totalItems = carrito.getDetalles().stream()
                    .filter(d -> !d.isEliminado())
                    .mapToInt(Detalle::getCantidad)
                    .sum();

            model.addAttribute("historial", historial);
            model.addAttribute("usuario", usuario);
            model.addAttribute("cantidadItemsCarrito", totalItems);

            return "checkout/historial";
        } catch (ErrorServicio e) {
            return "redirect:/catalogo";
        }
    }
}
