package com.example.pelis_api_externa.controllers;

import com.example.pelis_api_externa.model.Pelicula;
import com.example.pelis_api_externa.services.PeliculaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
//@RequestMapping
public class PeliculaController {

    private final PeliculaService peliculaService;

    @Autowired
    public PeliculaController(PeliculaService peliculaService) {
        this.peliculaService = peliculaService;
    }

    //vista principal
    @GetMapping("/ruleta")
    public String mostrarRuleta() {
        return "ruleta";
    }

    //formulario enviado por el usuario
    @GetMapping("/ruleta/buscar")
    public String buscarPeliculas(
            // 'anio' es obligatorio por defecto al no poner required=false
            @RequestParam(name = "anio") Integer anio,
            @RequestParam(name = "genero", required = false) String genero,
            Model model) {

        // llamamos a nuestro servicio pasándole los parámetros para que se comunique
        //con la api y obtenga las pelis que vamos a pasar a la vista
        List<Pelicula> resultados = peliculaService.buscarPeliculas(anio, genero);
        model.addAttribute("peliculas", resultados);

        return "ruleta";
    }

}
