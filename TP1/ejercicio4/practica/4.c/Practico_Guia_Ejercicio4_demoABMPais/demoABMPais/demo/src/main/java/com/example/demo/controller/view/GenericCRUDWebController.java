package com.example.demo.controller.view;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public abstract class GenericCRUDWebController <T extends Object>{

   	private String nameClass;
   	protected boolean campoDesactivado;
    protected Object object;
    
    //Vistas de rotorno para navegabilidad
	private String viewList;
	private String redirectList;
	private String viewEdit; 
   	
	//Constructor
    public GenericCRUDWebController(T object){
    	nameClass= getNameObject(object);
    	viewList= "view/l"+ nameClass +".html";
    	redirectList= "redirect:/list"+ nameClass;
    	viewEdit= "view/e"+ nameClass +".html";
    }
    
	private String getNameObject(T object){
        return ((((T) object).getClass()).getSimpleName());
    }

    private String getNameClass() {
        return nameClass;
    }
    
    private String getValueIdFieldObject(T object) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
    	Field field = null;
        try {
            field = object.getClass().getDeclaredField("id");
        } catch (Exception e) {
            field = object.getClass().getSuperclass().getDeclaredField("id");
        }
        field.setAccessible(true);
        String id= (String) field.get(object);
        return id;
    }
	
	/////////////////////////////////////////////
	/////////////////////////////////////////////
	///////////////// VIEW: Lista ///////////////
	/////////////////////////////////////////////
	/////////////////////////////////////////////

	/*@GetMapping, @PostMapping, @PutMapping, @DeleteMapping
    Propósito: Mapean los métodos de la clase a las solicitudes HTTP GET, POST, PUT y DELETE respectivamente.
    Función: Permiten definir rutas específicas y asociar los métodos del controlador a estas rutas.
    Esto facilita la creación de endpoints RESTful.*/

	/*@GetMapping se utiliza para asignar solicitudes
	 HTTP GET a métodos específicos de un controlador.*/

	/* Model: En el contexto de Spring Boot y su framework web, Spring MVC, el "Modelo" se refiere al componente
	 * responsable de transportar los datos y la lógica de negocio entre el controlador y la vista. Es un componente fundamental
	 * del patrón arquitectónico Modelo-Vista-Controlador (MVC).
	 */
	@GetMapping("/list")
	public String listTemplateMethod(Model model) {
		try {
			  
		  List<T> list = listObject();
		  model.addAttribute("list"+ getNameClass(), list);
 
		}catch(Exception e) {
		  model.addAttribute("msgError", e.getMessage());  
		}
		return viewList;
	}
	
	protected abstract List<T> listObject();
	
	/////////////////////////////////////////////
	/////////////////////////////////////////////
	////////////// VIEW: NAVEGACION /////////////
	/////////////////////////////////////////////
	/////////////////////////////////////////////

	/*@GetMapping se utiliza para asignar solicitudes
	 HTTP GET a métodos específicos de un controlador.*/
	@GetMapping("/edit")
	public String browsePageEdit(T object, Model model) {
		model.addAttribute("isDisabled", false);
		return viewEdit;
	}

	/*@GetMapping se utiliza para asignar solicitudes
	 HTTP GET a métodos específicos de un controlador.*/

	/* @PathVariable: Vincula una variable de ruta en la URL de la solicitud con un parámetro del método.
	 * Permite extraer valores de la URL y usarlos en el método del controlador.
	 * Por ejemplo, @PathVariable ID id vincula el valor del segmento {id} de la URL al parámetro id
	 */
	@GetMapping("")
	public String editTemplateMethod(@PathVariable("id") String id, Model model) {
		
		try {
			
		  T object = getObjectById(id);		
		  model.addAttribute("object"+ nameClass, object);
		  model.addAttribute("isDisabled", true);
		  
		  return viewEdit;
		 
		}catch(Exception e) {	
		  model.addAttribute("msgError", e.getMessage());
		  return viewList;
		}		  
	}
	
	protected abstract T getObjectById(String id);

	/*@GetMapping se utiliza para asignar solicitudes
	 HTTP GET a métodos específicos de un controlador.*/

	@GetMapping("")
	public String browsePageEditTemplateMethod(@PathVariable("id") String id, Model model) {
		
		try {
			
		  T object = getObjectById(id);		
	      model.addAttribute("object"+ nameClass, object);
		  model.addAttribute("isDisabled", false);
		  
		  return viewEdit;
		 
		}catch(Exception e) {	
		  model.addAttribute("msgError", e.getMessage());
		  return viewList;
		}		  
	}

	/*@GetMapping se utiliza para asignar solicitudes
	 HTTP GET a métodos específicos de un controlador.*/
	@GetMapping("")
	public String eliminateTemplateMethod(@PathVariable("id") String id, RedirectAttributes attributes, Model model) {	
		
		try {
			
		  eliminate(id);

			//addFlashAttribute es un método de la interfaz RedirectAttributes
			// extiende el tiempo de vida de los datos, con addAtributes solo viven durante la
			//petición, en cambio este método guarda el dato temporalmentnte en la sesion del
			//usuario
		  attributes.addFlashAttribute("msgExito", "La acción fue realizada correctamente.");
		  return redirectList;
		  
		}catch(Exception e) {	
		   model.addAttribute("msgError", e.getMessage());
		   return redirectList;
		} 
	}
	
	protected abstract void eliminate(String id);
	
	/////////////////////////////////////////////
	/////////////////////////////////////////////
	///////////////// VIEW: Edit ////////////////
	/////////////////////////////////////////////
	/////////////////////////////////////////////


	/* @PostMapping es una anotación específica de Spring utilizada para mapear solicitudes HTTP POST
	 * en métodos de un controlador. Esta anotación simplifica la configuración de rutas y es útil
	 * para operaciones que implican la creación o el envío de datos desde el cliente al servidor.
	 * A diferencia de @GetMapping, que se usa para recuperar información, @PostMapping suele utilizarse
	 * cuando se envían datos para ser procesados, como por ejemplo el envío de formularios o la creación de registros en bases de datos,
	 */
	@PostMapping("")
	public String acceptEditTemplateMethod(T object, BindingResult result, RedirectAttributes attributes, Model model){
		
		try {
			
		  if (result.hasErrors()){		
			model.addAttribute("msgError", "Error de Sistema");
			return viewEdit;
		  }
		 
		  executeUseCase(object);
			  
		  attributes.addFlashAttribute("msgExito", "La acción fue realizada correctamente.");
		  return redirectList;
		  
		}catch(Exception e) {
			  model.addAttribute("msgError", "Error de Sistema");
			  return viewEdit;
		}
		
	}
	
	protected abstract void executeUseCase(T object);

	/*@GetMapping se utiliza para asignar solicitudes
	 HTTP GET a métodos específicos de un controlador.*/
	@GetMapping("/cancelEdit")
	public String cancelEdit() {
		return redirectList;
	}
	

}
