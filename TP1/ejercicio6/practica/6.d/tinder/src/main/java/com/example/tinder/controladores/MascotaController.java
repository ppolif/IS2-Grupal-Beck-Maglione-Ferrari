package com.example.tinder.controladores;

import com.example.tinder.entidades.Mascota;
import com.example.tinder.entidades.Usuario;
import com.example.tinder.entidades.Zona;
import com.example.tinder.enumeraciones.Sexo;
import com.example.tinder.enumeraciones.Tipo;
import com.example.tinder.errores.ErrorServicio;
import com.example.tinder.servicios.MascotaServicio;
import com.example.tinder.servicios.UsuarioServicio;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@Controller
@RequestMapping("/mascota")
public class MascotaController {

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private MascotaServicio mascotaServicio;

    @GetMapping("/eliminar-perfil")
    public String eliminar(HttpSession session, @RequestParam String id){
        try {
            Usuario login = (Usuario)session.getAttribute("usuariosession");
            mascotaServicio.eliminarMascota(login.getId(), id);
        } catch (ErrorServicio e){
            throw new RuntimeException(e);
        }
        return "redirect:/mascota/mis-mascotas";
    }

    @GetMapping("/mis-mascotas")
    public String misMascotas( HttpSession session, ModelMap model) {
        Usuario login = (Usuario)session.getAttribute("usuariosession");
        if(login == null){
            return "redirect:/login";
        }
        List<Mascota> mascotas = mascotaServicio.buscarMascotasPorUsuario(login.getId());
        model.put("mascotas", mascotas);
        return "mascotas";
    }

    @GetMapping("/editar-perfil")
    public String editarPerfil( HttpSession session, @RequestParam(required = false) String accion, @RequestParam(required=false) String id, ModelMap model){

        if (accion == null){
            accion = "Crear";
        }

        Usuario login = (Usuario)session.getAttribute("usuariosession");
        if(login == null){
            return "redirect:/inicio";
        }
        Mascota mascota = new Mascota();
        if (id != null && !id.isEmpty()){
            try {
                mascota = mascotaServicio.buscarPorId(id);
            } catch (ErrorServicio e) {
                throw new RuntimeException(e);
            }
        }
        model.put("perfil", mascota);
        model.put("accion", accion);
        model.put("sexos", Sexo.values());
        model.put("tipos", Tipo.values());

        return "mascota";
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/actualizar-perfil")
    public String actualizar(ModelMap modelo, HttpSession session, MultipartFile archivo, @RequestParam String id, @RequestParam String nombre, @RequestParam Sexo sexo, @RequestParam Tipo tipo) {

        Usuario login = (Usuario) session.getAttribute("usuariosession");
        if(login == null){
            return "redirect:/inicio";
        }
        try {
            if (id == null || id.isEmpty()) {
                mascotaServicio.agregarMascota(archivo, login.getId(), nombre, sexo, tipo);
            } else {
                mascotaServicio.actualizar(archivo, login.getId(), id, nombre, sexo, tipo);
            }

            return "redirect:/inicio";

        } catch (ErrorServicio e) {
            Mascota mascota = new Mascota();
            mascota.setNombre(nombre);
            mascota.setTipo(tipo);
            mascota.setSexo(sexo);

            modelo.put("accion", "Actualizar");
            modelo.put("sexos", Sexo.values());
            modelo.put("tipos", Tipo.values());
            modelo.put("error", e.getMessage());
            modelo.put("perfil", mascota);
            return "mascota.html";

        }
    }

}
