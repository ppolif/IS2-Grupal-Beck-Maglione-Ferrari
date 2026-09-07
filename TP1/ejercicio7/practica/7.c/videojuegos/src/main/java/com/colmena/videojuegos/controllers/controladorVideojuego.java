package com.colmena.videojuegos.controllers;

import com.colmena.videojuegos.dtos.CategoriaResponseDTO;
import com.colmena.videojuegos.dtos.EstudioResponseDTO;
import com.colmena.videojuegos.dtos.VideojuegoRequestDTO;
import com.colmena.videojuegos.dtos.VideojuegoResponseDTO;
import com.colmena.videojuegos.services.ServicioCategoria;
import com.colmena.videojuegos.services.ServicioEstudio;
import com.colmena.videojuegos.services.ServicioVideojuego;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class controladorVideojuego {

    @Autowired
    private ServicioVideojuego servicioVideojuego;

    @Autowired
    private ServicioCategoria servicioCategoria;

    @Autowired
    private ServicioEstudio servicioEstudio;

    @GetMapping("/inicio")
    public String inicio(Model model) {
        try {
            List<VideojuegoResponseDTO> videojuegos = this.servicioVideojuego.listarDTO();
            model.addAttribute("videojuegos", videojuegos);
            return "views/inicio";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/detalle/{id}")
    public String detalle(Model model, @PathVariable("id") Long id) {
        try {
            VideojuegoResponseDTO videojuego = this.servicioVideojuego.buscarPorIdDTO(id);
            model.addAttribute("videojuego", videojuego);
            return "views/detalle";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/busqueda")
    public String busqueda(Model model, @RequestParam(value = "query", required = false) String q) {
        try {
            List<VideojuegoResponseDTO> videojuegos = this.servicioVideojuego.buscarPorTituloDTO(q);
            model.addAttribute("videojuegos", videojuegos);
            return "views/busqueda";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/crud")
    public String crud(Model model) {
        try {
            List<VideojuegoResponseDTO> videojuegos = this.servicioVideojuego.listarDTO();
            model.addAttribute("videojuegos", videojuegos);
            return "views/crud";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/formulario/videojuego/{id}")
    public String formularioVideojuego(Model model, @PathVariable("id") Long id) {
        try {
            List<CategoriaResponseDTO> categorias = this.servicioCategoria.listarDTO();
            List<EstudioResponseDTO> estudios = this.servicioEstudio.listarDTO();
            model.addAttribute("categorias", categorias);
            model.addAttribute("estudios", estudios);

            if (id == 0) {
                model.addAttribute("videojuego", new VideojuegoRequestDTO());
            } else {
                VideojuegoRequestDTO dto = this.servicioVideojuego.obtenerParaEdicion(id);
                model.addAttribute("videojuego", dto);
            }
            return "views/formulario/videojuego";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @PostMapping("/formulario/videojuego/{id}")
    public String guardarVideojuego(
            @Valid @ModelAttribute("videojuego") VideojuegoRequestDTO videojuegoDTO,
            BindingResult result,
            Model model,
            @PathVariable("id") Long id) {

        try {
            if (result.hasErrors()) {
                model.addAttribute("categorias", this.servicioCategoria.listarDTO());
                model.addAttribute("estudios", this.servicioEstudio.listarDTO());
                return "views/formulario/videojuego";
            }

            this.servicioVideojuego.guardarDesdeDTO(videojuegoDTO);
            return "redirect:/crud";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/formulario/eliminar/{id}")
    public String eliminarVideojuego(Model model, @PathVariable("id") Long id) {
        try {
            VideojuegoResponseDTO dto = this.servicioVideojuego.buscarPorIdDTO(id);
            model.addAttribute("videojuego", dto);
            return "views/formulario/eliminar";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @PostMapping("/formulario/eliminar/{id}")
    public String eliminarVideojuegoConfirmado(@PathVariable("id") Long id) {
        try {
            this.servicioVideojuego.deleteById(id);
            return "redirect:/crud";
        } catch (Exception e) {
            return "error";
        }
    }
}