package com.example.demo.controller.view;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.business.domain.entity.Nacionalidad;
import com.example.demo.business.domain.entity.Pais;
import com.example.demo.business.logic.error.ErrorServiceException;
import com.example.demo.business.logic.service.NacionalidadService;

/*@Controller: Indica a Spring que esta clase manejará las solicitudes HTTP entrantes
y actuará como intermediario entre el cliente y la lógica de negocio.*/
@Controller

//@RequestMapping de spring boot dirige las solicitudes HTTP a clases o métodos de Java
//específicos. cuando se usa a nivel de clase, define una ruta base de URL para
//todos los endpoints dentro de ese controlador
@RequestMapping("/nacionalidad")
public class NacionalidadController {

	//Es la herramiento que nos da Spring Boot para dar soporte al patrón de
	//inyeccion de dependencias. esta anotacion se coloca sobre los atributos
	//que referencian a otras clases para que Spring inyecte la dependencia y así
	//no tener que crear el objeto nosotros mismos
	@Autowired
   	private NacionalidadService nacionalidadService;

	/* redirect: En Spring Boot, la redirección es un mecanismo que redirige el navegador de un usuario
	   a una URL diferente. Esto se suele emplear después de acciones como el envío de formularios,
	   cuando se mueve un recurso o para dirigir a los usuarios a páginas específicas según la lógica.
	 */
	private String viewList="view/nacionalidad/lNacionalidad.html";
	private String redirectList= "redirect:/nacionalidad/listNacionalidad";
	private String viewEdit="view/nacionalidad/eNacionalidad.html";
	
	/////////////////////////////////////////////
	/////////////////////////////////////////////
	/////////// VIEW: lNacionalidad /////////////
	/////////////////////////////////////////////
	/////////////////////////////////////////////

	/*@GetMapping, @PostMapping, @PutMapping, @DeleteMapping
    Propósito: Mapean los métodos de la clase a las solicitudes HTTP GET, POST, PUT y DELETE respectivamente.
    Función: Permiten definir rutas específicas y asociar los métodos del controlador a estas rutas.
    Esto facilita la creación de endpoints RESTful.*/

	/*@GetMapping se utiliza para asignar solicitudes
	 HTTP GET a métodos específicos de un controlador.*/
	@GetMapping("/listNacionalidad")
	public String listarNacionalidad(Model model) {
		try {
			  
		  List<Nacionalidad> listaNacionalidad = nacionalidadService.listarNacionalidadActivo();

		  //La interfaz org.springframework.ui.Model es un contenedor que transfiere
		  //datos del controlador a la vista (como Thymeleaf).
		  //addAttribute es para poner información en el contenedor. el primer atributo
			//es un string y es el nombre con el que la vista reconocerá al dato, y el segundo
			//es el objeto real que queremos enviar a la vista

			/* Model: En el contexto de Spring Boot y su framework web, Spring MVC, el "Modelo" se refiere al componente
			 * responsable de transportar los datos y la lógica de negocio entre el controlador y la vista. Es un componente fundamental
			 * del patrón arquitectónico Modelo-Vista-Controlador (MVC).
			 */
		  model.addAttribute("listaNacionalidad", listaNacionalidad);

		}catch(ErrorServiceException e) {	
		  model.addAttribute("msgError", e.getMessage());  
		}catch(Exception e) {
		  model.addAttribute("msgError", "Error de Sistema");  
		}
		return viewList;
	}

	/*@GetMapping se utiliza para asignar solicitudes
	 HTTP GET a métodos específicos de un controlador.*/
	@GetMapping("/altaNacionalidad")
	public String alta(Nacionalidad nacionalidad, Model model) {
		model.addAttribute("isDisabled", false);
		return viewEdit;
	}

	/*@GetMapping se utiliza para asignar solicitudes
	 HTTP GET a métodos específicos de un controlador.*/
	@GetMapping("/consultar")
	//@RequestParam es la herramienta de Spring Boot para capturar los parametros
	// que el usuario envio a traves de la URL o desde un formulario HTML.
	public String consultar(@RequestParam(value="id") String idNacionalidad, Model model) {
		
		try {
			
		  Nacionalidad nacionalidad = nacionalidadService.buscarNacionalidad(idNacionalidad);		
		  model.addAttribute("nacionalidad", nacionalidad);
		  model.addAttribute("isDisabled", true);
		  
		  return viewEdit;
		 
		}catch(ErrorServiceException e) {	
		  model.addAttribute("msgError", e.getMessage());
		  return viewList;
		}		  
	}

	/*@GetMapping se utiliza para asignar solicitudes
	 HTTP GET a métodos específicos de un controlador.*/
	@GetMapping("/modificar")
	//@RequestParam es la herramienta de Spring Boot para capturar los parametros
	// que el usuario envio a traves de la URL o desde un formulario HTML.
	public String modificar(@RequestParam(value="id") String idNacionalidad, Model model) {
		
		try {
			
		  Nacionalidad nacionalidad = nacionalidadService.buscarNacionalidad(idNacionalidad);		
		  model.addAttribute("nacionalidad", nacionalidad);
		  model.addAttribute("isDisabled", false);
		  
		  return viewEdit;
		 
		}catch(ErrorServiceException e) {	
		  model.addAttribute("msgError", e.getMessage());
		  return viewList;
		}		  
	}

	/*@GetMapping se utiliza para asignar solicitudes
	 HTTP GET a métodos específicos de un controlador.*/
	@GetMapping("/baja")
	//@RequestParam es la herramienta de Spring Boot para capturar los parametros
	// que el usuario envio a traves de la URL o desde un formulario HTML.
	public String baja(@RequestParam(value="id") String idNacionalidad, RedirectAttributes attributes, Model model) {	
		
		try {
			
		  nacionalidadService.eliminarNacionalidad(idNacionalidad);

		  //addFlashAttribute es un método de la interfaz RedirectAttributes
			// extiende el tiempo de vida de los datos, con addAtributes solo viven durante la
			//petición, en cambio este método guarda el dato temporalmentnte en la sesion del
			//usuario
		  attributes.addFlashAttribute("msgExito", "La acción fue realizada correctamente.");
		  return redirectList;
		  
		}catch(ErrorServiceException e) {	
		   model.addAttribute("msgError", e.getMessage());
		   return redirectList;
		} 
	}
	
	/////////////////////////////////////////////
	/////////////////////////////////////////////
	//////////// VIEW: eNacionalidad ////////////
	/////////////////////////////////////////////
	/////////////////////////////////////////////

	/* @PostMapping es una anotación específica de Spring utilizada para mapear solicitudes HTTP POST
	 * en métodos de un controlador. Esta anotación simplifica la configuración de rutas y es útil
	 * para operaciones que implican la creación o el envío de datos desde el cliente al servidor.
	 * A diferencia de @GetMapping, que se usa para recuperar información, @PostMapping suele utilizarse
	 * cuando se envían datos para ser procesados, como por ejemplo el envío de formularios o la creación de registros en bases de datos,
	 */
	@PostMapping("/aceptarEditNacionalidad")
	//@RequestParam es la herramienta de Spring Boot para capturar los parametros
	// que el usuario envio a traves de la URL o desde un formulario HTML.
	public String aceptarEdit(@RequestParam(value="id") String idNacionalidad, @RequestParam(value="nombre") String nombreNacionalidad, RedirectAttributes attributes, Model model){
		
		try {
			
		  if (idNacionalidad == null || idNacionalidad.trim().isEmpty())
		   nacionalidadService.crearNacionalidad(nombreNacionalidad);
		  else 
		   nacionalidadService.modificarNacionalidad(idNacionalidad, nombreNacionalidad);
			  
		  attributes.addFlashAttribute("msgExito", "La acción fue realizada correctamente.");
		  return redirectList;
		  
		}catch(ErrorServiceException e) {
			  return error (e.getMessage(), model, idNacionalidad, nombreNacionalidad);
		}catch(Exception e) {
			  return error ("Error de Sistema", model, idNacionalidad, nombreNacionalidad);
		}
		
	}

	private String error (String mensaje, Model model, String id, String nombre) {
		try {
			
			model.addAttribute("msgError", mensaje);
			if (id != null && !id.trim().isEmpty()) {
			 model.addAttribute("nacionalidad", nacionalidadService.buscarNacionalidad(id));
			}else {
			  Nacionalidad nacionalidad = new Nacionalidad();
			  nacionalidad.setId("");
			  nacionalidad.setNombre(nombre);
			  model.addAttribute("nacionalidad",nacionalidad);	
			}
			
		}catch(Exception e) {}
		return viewEdit;
	}

	/*@GetMapping se utiliza para asignar solicitudes
	 HTTP GET a métodos específicos de un controlador.*/
	@GetMapping("/cancelarEditNacionalidad")
	public String cancelarEdit() {
		return redirectList;
	}
	

}
