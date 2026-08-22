package com.example.demo.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/*@Controller: Indica a Spring que esta clase manejará las solicitudes HTTP entrantes
y actuará como intermediario entre el cliente y la lógica de negocio.*/
@Controller
public class InicioController {

	/*@GetMapping, @PostMapping, @PutMapping, @DeleteMapping
    Propósito: Mapean los métodos de la clase a las solicitudes HTTP GET, POST, PUT y DELETE respectivamente.
    Función: Permiten definir rutas específicas y asociar los métodos del controlador a estas rutas.
    Esto facilita la creación de endpoints RESTful.*/

	/*@GetMapping se utiliza para asignar solicitudes
	 HTTP GET a métodos específicos de un controlador.*/
	@GetMapping("/")
	public String inicio() {
		return "view/inicio";
	}
}
