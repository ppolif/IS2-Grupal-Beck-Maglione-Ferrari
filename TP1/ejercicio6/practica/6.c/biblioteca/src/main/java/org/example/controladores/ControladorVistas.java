package org.example.controladores;

import jakarta.validation.Valid;
import org.example.dtos.*;
import org.example.servicios.UsuarioServicioImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.example.servicios.LibroServicio;
import org.example.servicios.PrestamoServicio;
import org.example.repositorios.UsuarioRepositorio;
import org.example.entidades.Usuario;
import org.springframework.security.core.Authentication;
import org.example.entidades.enumeraciones.Rol;
import org.example.servicios.AutorServicio;
import org.example.servicios.EditorialServicio;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.example.servicios.ImagenServicio;
import org.example.dtos.ImagenResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.stream.Collectors;

@Controller
public class ControladorVistas {

    @Autowired
    private UsuarioServicioImpl usuarioServicio;

    @Autowired
    private LibroServicio libroServicio;

    @Autowired
    private PrestamoServicio prestamoServicio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio; // Lo usamos para obtener el ID del usuario logueado

    @Autowired
    private AutorServicio autorServicio;

    @Autowired
    private EditorialServicio editorialServicio;

    // --- CRUD DE LIBROS  ---

    @Autowired
    private ImagenServicio imagenServicio;

    @GetMapping("/libros/nuevo")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String mostrarFormularioNuevoLibro(Model model) {
        model.addAttribute("libroDTO", new LibroRequestDTO());
        model.addAttribute("editoriales", editorialServicio.obtenerTodasActivas());
        model.addAttribute("autores", autorServicio.obtenerTodosActivos());
        return "formulario-libro";
    }

    // MODIFICADO PARA RECIBIR LA IMAGEN
    @PostMapping("/libros/nuevo")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String procesarNuevoLibro(@Valid @ModelAttribute("libroDTO") LibroRequestDTO libroDTO,
                                     BindingResult result,
                                     @RequestParam(value = "archivo", required = false) MultipartFile archivo,
                                     Model model) {
        if (result.hasErrors()) {
            model.addAttribute("editoriales", editorialServicio.obtenerTodasActivas());
            model.addAttribute("autores", autorServicio.obtenerTodosActivos());
            return "formulario-libro";
        }
        try {
            // Lógica de Imagen: Si subieron un archivo, lo guardamos y le pasamos el ID al libro
            if (archivo != null && !archivo.isEmpty()) {
                ImagenResponseDTO imagenDTO = imagenServicio.guardar(archivo);
                libroDTO.setIdImagen(imagenDTO.getId());
            }

            libroServicio.crear(libroDTO);
            return "redirect:/libros?exito=Libro registrado correctamente";
        } catch (Exception e) {
            model.addAttribute("errorNegocio", e.getMessage());
            model.addAttribute("editoriales", editorialServicio.obtenerTodasActivas());
            model.addAttribute("autores", autorServicio.obtenerTodosActivos());
            return "formulario-libro";
        }
    }

    @GetMapping("/libros/editar/{isbn}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String mostrarFormularioEditarLibro(@PathVariable Long isbn, Model model) {
        LibroResponseDTO libroExistente = libroServicio.obtenerPorIsbn(isbn);

        // Mapeamos los datos existentes al DTO del formulario
        LibroRequestDTO libroDTO = new LibroRequestDTO();
        libroDTO.setIsbn(libroExistente.getIsbn());
        libroDTO.setTitulo(libroExistente.getTitulo());
        libroDTO.setAnio(libroExistente.getAnio());
        libroDTO.setEjemplares(libroExistente.getEjemplares());
        libroDTO.setIdEditorial(libroExistente.getEditorial().getId());
        libroDTO.setIdAutores(libroExistente.getAutores().stream().map(a -> a.getId()).collect(Collectors.toList()));

        // Mapeamos el ID de la imagen actual si es que tiene una
        if (libroExistente.getImagen() != null) {
            libroDTO.setIdImagen(libroExistente.getImagen().getId());
        }

        model.addAttribute("libroDTO", libroDTO);
        model.addAttribute("editoriales", editorialServicio.obtenerTodasActivas());
        model.addAttribute("autores", autorServicio.obtenerTodosActivos());
        model.addAttribute("modoEdicion", true); // Bandera para reutilizar la vista
        return "formulario-libro";
    }

    // MODIFICADO PARA ACTUALIZAR LA IMAGEN
    @PostMapping("/libros/editar/{isbn}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String procesarEditarLibro(@PathVariable Long isbn,
                                      @Valid @ModelAttribute("libroDTO") LibroRequestDTO libroDTO,
                                      BindingResult result,
                                      @RequestParam(value = "archivo", required = false) MultipartFile archivo,
                                      Model model) {
        if (result.hasErrors()) {
            model.addAttribute("editoriales", editorialServicio.obtenerTodasActivas());
            model.addAttribute("autores", autorServicio.obtenerTodosActivos());
            model.addAttribute("modoEdicion", true);
            return "formulario-libro";
        }
        try {
            // Lógica de Imagen en Edición
            if (archivo != null && !archivo.isEmpty()) {
                if (libroDTO.getIdImagen() != null && !libroDTO.getIdImagen().isEmpty()) {
                    // Si ya tenía imagen, la pisamos (actualizamos)
                    imagenServicio.actualizar(libroDTO.getIdImagen(), archivo);
                } else {
                    // Si no tenía, creamos una nueva y la vinculamos
                    ImagenResponseDTO imagenDTO = imagenServicio.guardar(archivo);
                    libroDTO.setIdImagen(imagenDTO.getId());
                }
            }

            libroServicio.actualizar(isbn, libroDTO);
            return "redirect:/libros?exito=Libro actualizado correctamente";
        } catch (Exception e) {
            model.addAttribute("errorNegocio", e.getMessage());
            model.addAttribute("editoriales", editorialServicio.obtenerTodasActivas());
            model.addAttribute("autores", autorServicio.obtenerTodosActivos());
            model.addAttribute("modoEdicion", true);
            return "formulario-libro";
        }
    }

    @PostMapping("/libros/eliminar/{isbn}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String eliminarLibro(@PathVariable Long isbn) {
        libroServicio.darDeBaja(isbn);
        return "redirect:/libros?exito=Libro eliminado correctamente";
    }

    // --- RUTAS PARA EL CRUD ---

    @GetMapping("/libros")
    public String listarLibros(Model model) {
        // Pasamos la lista de libros activos a la vista
        model.addAttribute("libros", libroServicio.obtenerTodosActivos());
        return "libros";
    }

    @GetMapping("/prestamos")
    public String listarPrestamos(Model model, Authentication authentication) {
        // Verificamos si el usuario logueado es administrador
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));

        if (isAdmin) {
            // El admin ve TODOS los préstamos
            model.addAttribute("prestamos", prestamoServicio.obtenerTodosActivos());
        } else {
            // El usuario normal solo ve SUS préstamos activos
            Usuario usuario = usuarioRepositorio.findByMailAndAltaTrue(authentication.getName()).orElseThrow();
            model.addAttribute("prestamos", prestamoServicio.obtenerPrestamosActivosDeUsuario(usuario.getId()));
        }
        return "prestamos";
    }
    @GetMapping({"/", "/index"})
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                        @RequestParam(required = false) String logout,
                        Model model) {
        if (error != null) {
            model.addAttribute("error", "Email o contraseña incorrectos.");
        }
        if (logout != null) {
            model.addAttribute("exito", "Has cerrado sesión exitosamente.");
        }
        return "login";
    }

    // Muestra el formulario de registro y pasa un DTO vacío
    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("usuarioDTO", new UsuarioRequestDTO());
        return "registro";
    }

    // Procesa el POST del formulario de registro
    @PostMapping("/registro")
    public String procesarRegistro(@Valid @ModelAttribute("usuarioDTO") UsuarioRequestDTO usuarioDTO,
                                   BindingResult result,
                                   Model model) {
        if (result.hasErrors()) {
            return "registro";
        }

        try {
            usuarioServicio.registrarUsuario(usuarioDTO);
            return "redirect:/login?exito=true";

        } catch (IllegalArgumentException e) {
            model.addAttribute("errorNegocio", e.getMessage());
            return "registro";
        }
    }

    @GetMapping("/inicio")
    public String inicio() {
        return "inicio";
    }

    @GetMapping("/403")
    public String accesoDenegado() {
        return "error/403";
    }

    // --- LÓGICA DE PRÉSTAMOS (VISTAS MVC) ---

    @PostMapping("/prestamos/solicitar/{isbn}")
    @PreAuthorize("hasRole('USUARIO')")
    public String solicitarPrestamo(@PathVariable Long isbn, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            // Obtenemos el usuario autenticado actualmente
            Usuario usuario = usuarioRepositorio.findByMailAndAltaTrue(authentication.getName())
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

            // Armamos el DTO
            PrestamoRequestDTO dto = new PrestamoRequestDTO();
            dto.setIsbnLibro(isbn);
            dto.setIdUsuario(usuario.getId());

            // Registramos el préstamo
            prestamoServicio.registrarPrestamo(dto);

            // Usamos RedirectAttributes para pasar el mensaje a la URL redirigida
            redirectAttributes.addAttribute("exito", "Préstamo solicitado con éxito. ¡Disfruta tu lectura!");

        } catch (IllegalStateException | IllegalArgumentException e) {
            // Captura si no hay stock o si ya tiene el libro
            redirectAttributes.addAttribute("error", e.getMessage());
        }

        return "redirect:/libros";


    }

    @PostMapping("/prestamos/devolver/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String registrarDevolucion(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            prestamoServicio.registrarDevolucion(id);
            redirectAttributes.addAttribute("exito", "Devolución registrada correctamente. El stock del libro ha sido restaurado.");
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", e.getMessage());
        }
        return "redirect:/prestamos";
    }

    // --- PANEL DE GESTIÓN INTERNA ---

    @GetMapping("/gestion")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String panelGestion() {
        return "gestion"; // Vista que funcionará como menú de opciones
    }

    // --- CRUD DE AUTORES (MVC) ---

    @GetMapping("/autores")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String listarAutores(Model model) {
        model.addAttribute("autores", autorServicio.obtenerTodosActivos());
        return "autores";
    }

    @GetMapping("/autores/nuevo")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String mostrarFormularioAutor(Model model) {
        model.addAttribute("autorDTO", new AutorRequestDTO());
        return "formulario-autor";
    }

    @PostMapping("/autores/nuevo")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String procesarNuevoAutor(@Valid @ModelAttribute("autorDTO") AutorRequestDTO autorDTO,
                                     BindingResult result) {
        if (result.hasErrors()) {
            return "formulario-autor";
        }
        autorServicio.crear(autorDTO);
        return "redirect:/autores?exito=Autor registrado correctamente";
    }

    @GetMapping("/autores/editar/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String mostrarFormularioEditarAutor(@PathVariable String id, Model model) {
        // Buscamos el autor y pasamos el nombre al DTO
        AutorRequestDTO dto = new AutorRequestDTO();
        dto.setNombre(autorServicio.obtenerPorId(id).getNombre());

        model.addAttribute("autorDTO", dto);
        model.addAttribute("idAutor", id); // Necesario para la URL del formulario
        model.addAttribute("modoEdicion", true);
        return "formulario-autor";
    }

    @PostMapping("/autores/editar/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String procesarEditarAutor(@PathVariable String id,
                                      @Valid @ModelAttribute("autorDTO") AutorRequestDTO autorDTO,
                                      BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("modoEdicion", true);
            model.addAttribute("idAutor", id);
            return "formulario-autor";
        }
        autorServicio.actualizar(id, autorDTO);
        return "redirect:/autores?exito=Autor actualizado correctamente";
    }

    @PostMapping("/autores/eliminar/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String eliminarAutor(@PathVariable String id) {
        autorServicio.darDeBaja(id);
        return "redirect:/autores?exito=Autor eliminado correctamente";
    }

    // --- CRUD DE EDITORIALES (MVC) ---

    @GetMapping("/editoriales")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String listarEditoriales(Model model) {
        model.addAttribute("editoriales", editorialServicio.obtenerTodasActivas());
        return "editoriales";
    }

    @GetMapping("/editoriales/nuevo")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String mostrarFormularioEditorial(Model model) {
        model.addAttribute("editorialDTO", new EditorialRequestDTO());
        return "formulario-editorial";
    }

    @PostMapping("/editoriales/nuevo")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String procesarNuevaEditorial(@Valid @ModelAttribute("editorialDTO") EditorialRequestDTO editorialDTO,
                                         BindingResult result) {
        if (result.hasErrors()) {
            return "formulario-editorial";
        }
        editorialServicio.crear(editorialDTO);
        return "redirect:/editoriales?exito=Editorial registrada correctamente";
    }

    @GetMapping("/editoriales/editar/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String mostrarFormularioEditarEditorial(@PathVariable String id, Model model) {
        // Buscamos la editorial y pasamos el nombre al DTO
        EditorialRequestDTO dto = new EditorialRequestDTO();
        dto.setNombre(editorialServicio.obtenerPorId(id).getNombre());

        model.addAttribute("editorialDTO", dto);
        model.addAttribute("idEditorial", id); // Pasamos el ID correcto para la vista
        model.addAttribute("modoEdicion", true);
        return "formulario-editorial";
    }

    @PostMapping("/editoriales/editar/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String procesarEditarEditorial(@PathVariable String id,
                                          @Valid @ModelAttribute("editorialDTO") EditorialRequestDTO editorialDTO,
                                          BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("modoEdicion", true);
            model.addAttribute("idEditorial", id);
            return "formulario-editorial";
        }
        editorialServicio.actualizar(id, editorialDTO);
        return "redirect:/editoriales?exito=Editorial actualizada correctamente";
    }

    @PostMapping("/editoriales/eliminar/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String eliminarEditorial(@PathVariable String id) {
        editorialServicio.darDeBaja(id);
        return "redirect:/editoriales?exito=Editorial eliminada correctamente";
    }

    // --- CRUD DE USUARIOS (MVC) ---

    @GetMapping("/usuarios")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioServicio.obtenerTodosActivos());
        return "usuarios";
    }

    @GetMapping("/usuarios/nuevo")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String mostrarFormularioUsuario(Model model) {
        model.addAttribute("usuarioDTO", new UsuarioRequestDTO());
        return "formulario-usuario";
    }

    @PostMapping("/usuarios/nuevo")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String procesarNuevoUsuario(@Valid @ModelAttribute("usuarioDTO") UsuarioRequestDTO usuarioDTO,
                                       BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "formulario-usuario";
        }
        try {
            usuarioServicio.registrarUsuario(usuarioDTO);
            return "redirect:/usuarios?exito=Usuario registrado correctamente";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorNegocio", e.getMessage());
            return "formulario-usuario";
        }
    }

    @GetMapping("/usuarios/editar/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String mostrarFormularioEditarUsuario(@PathVariable String id, Model model) {
        UsuarioResponseDTO usuarioExistente = usuarioServicio.obtenerPorId(id);

        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setDni(usuarioExistente.getDni());
        dto.setNombre(usuarioExistente.getNombre());
        dto.setTelefono(usuarioExistente.getTelefono());
        dto.setMail(usuarioExistente.getMail());
        // La clave se deja vacía por seguridad, el admin deberá ingresar una nueva o repetir la actual

        model.addAttribute("usuarioDTO", dto);
        model.addAttribute("idUsuario", id);
        model.addAttribute("modoEdicion", true);
        return "formulario-usuario";
    }

    @PostMapping("/usuarios/editar/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String procesarEditarUsuario(@PathVariable String id,
                                        @Valid @ModelAttribute("usuarioDTO") UsuarioRequestDTO usuarioDTO,
                                        BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("modoEdicion", true);
            model.addAttribute("idUsuario", id);
            return "formulario-usuario";
        }
        try {
            usuarioServicio.actualizar(id, usuarioDTO);
            return "redirect:/usuarios?exito=Usuario actualizado correctamente";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorNegocio", e.getMessage());
            model.addAttribute("modoEdicion", true);
            model.addAttribute("idUsuario", id);
            return "formulario-usuario";
        }
    }

    @PostMapping("/usuarios/eliminar/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String eliminarUsuario(@PathVariable String id) {
        usuarioServicio.darDeBaja(id);
        return "redirect:/usuarios?exito=Usuario dado de baja correctamente";
    }


}