package com.example.carrito.controladores;

import com.example.carrito.entidades.Articulo;
import com.example.carrito.entidades.Carrito;
import com.example.carrito.entidades.Usuario;
import com.example.carrito.excepciones.ErrorServicio;
import com.example.carrito.servicios.ArticuloServicio;
import com.example.carrito.servicios.CarritoServicio;
import com.example.carrito.servicios.UsuarioServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;
import java.util.List;

@Controller
public class CatalogoControlador {

    @Autowired
    private ArticuloServicio articuloServicio;

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private CarritoServicio carritoServicio;

    @GetMapping({"/", "/catalogo"})
    public String listarCatalogo(Model model, Principal principal) {
        List<Articulo> articulos = articuloServicio.listarActivos();
        model.addAttribute("articulos", articulos);
        cargarDatosSesion(model, principal);
        return "catalogo/index";
    }

    @GetMapping("/articulo/{id}")
    public String verDetalle(@PathVariable String id, Model model, Principal principal) {
        try {
            Articulo articulo = articuloServicio.buscarPorId(id);
            model.addAttribute("articulo", articulo);
            cargarDatosSesion(model, principal);
            return "catalogo/detalle";
        } catch (ErrorServicio e) {
            return "redirect:/catalogo?error=" + e.getMessage();
        }
    }

    private void cargarDatosSesion(Model model, Principal principal) {
        if (principal != null) {
            try {
                Usuario usuario = usuarioServicio.buscarPorEmail(principal.getName());
                Carrito carrito = carritoServicio.obtenerOCrearCarritoActivo(usuario);
                model.addAttribute("usuarioLogueado", usuario);
                model.addAttribute("carritoActivo", carrito);
                
                int totalItems = carrito.getDetalles().stream()
                        .filter(d -> !d.isEliminado())
                        .mapToInt(d -> d.getCantidad())
                        .sum();
                model.addAttribute("cantidadItemsCarrito", totalItems);
            } catch (Exception ignored) {
            }
        }
    }
}

