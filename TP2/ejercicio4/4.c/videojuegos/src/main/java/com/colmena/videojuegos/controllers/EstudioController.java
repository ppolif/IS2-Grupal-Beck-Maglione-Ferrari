package com.colmena.videojuegos.controllers;

import com.colmena.videojuegos.entities.Estudio;
import com.colmena.videojuegos.services.ServicioEstudio;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/estudios")
public class EstudioController extends BaseControllerImpl<Estudio, ServicioEstudio> {
}
