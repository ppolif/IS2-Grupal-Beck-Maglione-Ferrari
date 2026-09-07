package com.example.tinder.controladores;

import com.example.tinder.dto.MascotaEdicionDTO;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@PreAuthorize("hasAnyRole('USUARIO_REGISTRADO')")
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
    public String editarPerfil(HttpSession session, @RequestParam(required = false) String accion, @RequestParam(required=false) String id, ModelMap model){

        if (accion == null){
            accion = "Crear";
        }

        Usuario login = (Usuario)session.getAttribute("usuariosession");
        if(login == null){
            return "redirect:/inicio";
        }

        MascotaEdicionDTO dto = new MascotaEdicionDTO();

        if (id != null && !id.isEmpty()){
            try {
                Mascota mascota = mascotaServicio.buscarPorId(id);
                dto.setId(mascota.getId());
                dto.setNombre(mascota.getNombre());
                dto.setSexo(mascota.getSexo());
                dto.setTipo(mascota.getTipo());
                dto.setTieneFoto(mascota.getFoto() != null);
            } catch (ErrorServicio e) {
                throw new RuntimeException(e);
            }
        } else {
            dto.setTieneFoto(false);
        }

        model.put("mascotaEdicionDTO", dto);

        model.put("accion", accion);
        model.put("sexos", Sexo.values());
        model.put("tipos", Tipo.values());

        return "mascota";
    }

    @PreAuthorize("hasAnyRole('USUARIO_REGISTRADO')")
    @PostMapping("/actualizar-perfil")
    public String actualizar(ModelMap modelo, HttpSession session, @RequestParam(required = false) String accion, @ModelAttribute MascotaEdicionDTO dto) {

        Usuario login = (Usuario) session.getAttribute("usuariosession");
        if(login == null){
            return "redirect:/inicio";
        }

        try {
            if (dto.getId() == null || dto.getId().isEmpty()) {
                mascotaServicio.agregarMascota(login.getId(), dto);
            } else {
                mascotaServicio.actualizar(login.getId(), dto);
            }
            return "redirect:/inicio";

        } catch (ErrorServicio e) {
            if (accion == null || accion.isEmpty()) { accion = "Actualizar"; }

            modelo.put("accion", accion);
            modelo.put("sexos", Sexo.values());
            modelo.put("tipos", Tipo.values());
            modelo.put("error", e.getMessage());
            modelo.put("mascotaEdicionDTO", dto);

            return "mascota.html";
        }
    }


}
