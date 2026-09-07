package com.example.tinder.controladores;

import com.example.tinder.dto.UsuarioEdicionDTO;
import com.example.tinder.entidades.Usuario;
import com.example.tinder.entidades.Zona;
import com.example.tinder.errores.ErrorServicio;
import com.example.tinder.repositorios.ZonaRepositorio;
import com.example.tinder.servicios.UsuarioServicio;
import jakarta.mail.Multipart;
import jakarta.servlet.http.HttpSession;
import org.hibernate.internal.build.AllowSysOut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/usuario")
public class UsuarioController {

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private ZonaRepositorio zonaRepositorio;

    @PreAuthorize("hasAnyRole('USUARIO_REGISTRADO')")
    @GetMapping("/editar-perfil")
    public String editarPerfil(HttpSession session, @RequestParam String id, ModelMap model){

        List<Zona> zonas = zonaRepositorio.findAll();
        model.put("zonas", zonas);

        Usuario login = (Usuario) session.getAttribute("usuariosession");
        if(login == null || !login.getId().equals(id)){
            return "redirect:/inicio";
        }

        try {
            Usuario usuario = usuarioServicio.buscarPorId(id);

            UsuarioEdicionDTO dto = new UsuarioEdicionDTO();
            dto.setId(usuario.getId());
            dto.setNombre(usuario.getNombre());
            dto.setApellido(usuario.getApellido());
            dto.setMail(usuario.getEmail());

            if (usuario.getZona() != null) {
                dto.setIdZona(usuario.getZona().getId());
            }

            // Asignamos el booleano para que la vista sepa si dibujar la etiqueta <img>
            dto.setTieneFoto(usuario.getFoto() != null);

            model.addAttribute("usuarioEdicionDTO", dto);

        } catch (ErrorServicio e){
            model.addAttribute("error", e.getMessage());
        }
        return "perfil.html";
    }

    @PreAuthorize("hasAnyRole('USUARIO_REGISTRADO')")
    @PostMapping("/actualizar-perfil")
    public String actualizarPerfil(ModelMap modelo, HttpSession session, @ModelAttribute UsuarioEdicionDTO dto){
        try {
            Usuario login = (Usuario) session.getAttribute("usuariosession");
            if(login == null || !login.getId().equals(dto.getId())){
                return "redirect:/inicio";
            }

            // Pasamos el paquete completo directamente al Servicio
            usuarioServicio.modificar(dto);

            session.setAttribute("usuariosession", usuarioServicio.buscarPorId(dto.getId()));
            return "redirect:/inicio";

        } catch (ErrorServicio e) {
            List<Zona> zonas = zonaRepositorio.findAll();
            modelo.put("zonas", zonas);
            modelo.put("error", e.getMessage());
            modelo.put("usuarioEdicionDTO", dto);

            return "perfil.html";
        }
    }
}
