package com.colmena.videojuegos.controllers;

import com.colmena.videojuegos.entities.Categoria;
import com.colmena.videojuegos.services.ServicioCategoria;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/categorias")
public class CategoriaController extends BaseControllerImpl<Categoria, ServicioCategoria> {
}
