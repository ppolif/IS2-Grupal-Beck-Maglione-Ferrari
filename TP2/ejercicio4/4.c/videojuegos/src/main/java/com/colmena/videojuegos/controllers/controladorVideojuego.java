package com.colmena.videojuegos.controllers;

import com.colmena.videojuegos.entities.Videojuego;
import com.colmena.videojuegos.services.FileStorageService;
import com.colmena.videojuegos.services.ServicioCategoria;
import com.colmena.videojuegos.services.ServicioEstudio;
import com.colmena.videojuegos.services.ServicioVideojuego;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@Controller
public class controladorVideojuego {

    @Autowired
    private ServicioVideojuego svcVideojuego;

    @Autowired
    private ServicioCategoria svcCategoria;

    @Autowired
    private ServicioEstudio svcEstudio;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("/inicio")
    public String inicio(Model model) {
        try {
            List<Videojuego> videojuegos = this.svcVideojuego.findAllByActivo();
            model.addAttribute("videojuegos", videojuegos);
            return "views/inicio";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/detalle/{id}")
    public String detalleVideojuego(Model model, @PathVariable("id") long id) {
        try {
            Videojuego videojuego = this.svcVideojuego.findByIdAndActivo(id);
            model.addAttribute("videojuego", videojuego);
            return "views/detalle";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @GetMapping(value = "/busqueda")
    public String busquedaVideojuego(Model model, @RequestParam(value = "query", required = false) String q) {
        try {
            List<Videojuego> videojuegos = this.svcVideojuego.findByTitle(q);
            model.addAttribute("videojuegos", videojuegos);
            model.addAttribute("resultado", q);
            return "views/busqueda";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/crud")
    public String crudVideojuego(Model model) {
        try {
            List<Videojuego> videojuegos = this.svcVideojuego.findAll();
            model.addAttribute("videojuegos", videojuegos);
            return "views/crud";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/formulario/videojuego/{id}")
    public String formularioVideojuego(Model model, @PathVariable("id") long id) {
        try {
            model.addAttribute("categorias", this.svcCategoria.findAll());
            model.addAttribute("estudios", this.svcEstudio.findAll());
            if (id == 0) {
                model.addAttribute("videojuego", new Videojuego());
            } else {
                model.addAttribute("videojuego", this.svcVideojuego.findById(id));
            }
            return "views/formulario/videojuego";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @PostMapping("/formulario/videojuego/{id}")
    public String guardarVideojuego(
            @RequestParam("archivo") MultipartFile archivo,
            @Valid @ModelAttribute("videojuego") Videojuego videojuego,
            BindingResult result,
            Model model, @PathVariable("id") long id
    ) {
        try {
            model.addAttribute("categorias", this.svcCategoria.findAll());
            model.addAttribute("estudios", this.svcEstudio.findAll());

            if (result.hasErrors()) {
                return "views/formulario/videojuego";
            }

            if (id == 0) {
                // Nuevo videojuego: la imagen es requerida
                if (archivo == null || archivo.isEmpty()) {
                    model.addAttribute("errorImagenMsg", "La imagen es requerida");
                    return "views/formulario/videojuego";
                }
                if (!this.validarExtension(archivo)) {
                    model.addAttribute("errorImagenMsg", "El formato de imagen no es válido (se admiten JPG, PNG, GIF, WebP)");
                    return "views/formulario/videojuego";
                }
                if (archivo.getSize() >= 15000000) {
                    model.addAttribute("errorImagenMsg", "El peso de la imagen excede los 15MB");
                    return "views/formulario/videojuego";
                }

                String nombreFoto = this.fileStorageService.guardarArchivo(archivo, null);
                videojuego.setImagen(nombreFoto);
                this.svcVideojuego.saveOne(videojuego);
            } else {
                // Actualización de videojuego existente
                Videojuego existente = this.svcVideojuego.findById(id);
                String nombreFoto = (existente != null) ? existente.getImagen() : videojuego.getImagen();

                if (archivo != null && !archivo.isEmpty()) {
                    if (!this.validarExtension(archivo)) {
                        model.addAttribute("errorImagenMsg", "El formato de imagen no es válido (se admiten JPG, PNG, GIF, WebP)");
                        return "views/formulario/videojuego";
                    }
                    if (archivo.getSize() >= 15000000) {
                        model.addAttribute("errorImagenMsg", "El peso de la imagen excede los 15MB");
                        return "views/formulario/videojuego";
                    }
                    nombreFoto = this.fileStorageService.guardarArchivo(archivo, nombreFoto);
                }

                videojuego.setImagen(nombreFoto);
                this.svcVideojuego.updateOne(videojuego, id);
            }
            return "redirect:/crud";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/eliminar/videojuego/{id}")
    public String eliminarVideojuego(Model model, @PathVariable("id") long id) {
        try {
            model.addAttribute("videojuego", this.svcVideojuego.findById(id));
            return "views/formulario/eliminar";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @PostMapping("/eliminar/videojuego/{id}")
    public String desactivarVideojuego(Model model, @PathVariable("id") long id) {
        try {
            this.svcVideojuego.deleteById(id);
            return "redirect:/crud";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    public boolean validarExtension(MultipartFile archivo) {
        return this.fileStorageService.validarExtension(archivo);
    }
}
