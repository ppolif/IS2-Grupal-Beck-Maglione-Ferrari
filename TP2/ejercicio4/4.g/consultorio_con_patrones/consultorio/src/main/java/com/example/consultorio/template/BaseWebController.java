package com.example.consultorio.template;

import com.example.consultorio.entities.EntidadPersona;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

//esta plantilla es para no repetir el codigo de las vistas del CRUD de Paciente y Medico
//ya que es el mismo
public abstract class BaseWebController<T extends EntidadPersona> {

    protected final TemplateCrud<T> service;
    protected String viewList;
    protected String viewEdit;
    protected String redirectList;
    protected String titleList;
    protected String titleEdit;

    //recibe la plantilla TemplateCrud<T>, el nombre de la carpeta de vistas
    // (entityFolder puede ser paciente o medico) y los títulos
    // a partir de entityFolder arma automáticamente las rutas de thymeleaf
    //esto es asi porque usamos generico que puede ser paciente y medico pero las vistas no son las mismas
    protected BaseWebController(TemplateCrud<T> service, String entityFolder, String titleList, String titleEdit) {
        this.service = service;
        this.viewList = entityFolder + "/lista";
        this.viewEdit = entityFolder + "/formulario";
        this.redirectList = "redirect:/" + entityFolder + "/list";
        this.titleList = titleList;
        this.titleEdit = titleEdit;
    }


    //metodo para listar las entidades usando la vista que corresponda
    @GetMapping("/list")
    public String list(Model model) {
        try {
            preUseCase(BaseUseCaseController.LISTAR, null, model);
            model.addAttribute("items", service.listarActivos());
            model.addAttribute("titleList", titleList);
            return viewList;
        } catch (Exception e) {
            model.addAttribute("msgError", e.getMessage());
            return viewList;
        }
    }

    // metodo plantilla para preparar alta
    @GetMapping("/alta")
    public String alta(Model model) {
        T entidad = service.instanciarNuevaEntidad();
        model.addAttribute("item", entidad);
        model.addAttribute("titleEdit", titleEdit);
        model.addAttribute("isDisabled", false);
        preUseCase(BaseUseCaseController.ALTA, entidad, model);
        return viewEdit;
    }

    // metodo plantilla para consultar
    @GetMapping("/consultar/{id}")
    public String consultar(@PathVariable String id, Model model) {
        T entidad = service.buscarPorId(id);
        model.addAttribute("item", entidad);
        model.addAttribute("titleEdit", "Consultar " + titleEdit);
        model.addAttribute("isDisabled", true);
        preUseCase(BaseUseCaseController.CONSULTAR, entidad, model);
        return viewEdit;
    }

    // metodo plantilla para editar
    @GetMapping("/modificar/{id}")
    public String editar(@PathVariable String id, Model model) {
        T entidad = service.buscarPorId(id);
        model.addAttribute("item", entidad);
        model.addAttribute("titleEdit", "Modificar " + titleEdit);
        model.addAttribute("isDisabled", false);
        preUseCase(BaseUseCaseController.MODIFICACION, entidad, model);
        return viewEdit;
    }

    // metodo plantilla para baja
    @GetMapping("/baja/{id}")
    public String baja(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            service.ejecutarBaja(id);
            redirectAttributes.addFlashAttribute("msgExito", "El registro fue dado de baja correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("msgError", e.getMessage());
        }
        return redirectList;
    }

    //este metodo preUseCase es un hook, que son  métodos vacíos opcionales
    // sirven por si en el futuro un formulario necesita cargar datos adicionales en el model antes de abrir la vista
    protected void preUseCase(BaseUseCaseController useCase, T entidad, Model model) {}
}
