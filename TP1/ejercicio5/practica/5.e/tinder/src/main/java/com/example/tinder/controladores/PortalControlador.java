package com.example.tinder.controladores;


import com.example.tinder.dto.UsuarioRegistroDTO;
import com.example.tinder.entidades.Zona;
import com.example.tinder.errores.ErrorServicio;
import com.example.tinder.repositorios.ZonaRepositorio;
import com.example.tinder.servicios.UsuarioServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
@RequestMapping("/")
public class PortalControlador {

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private ZonaRepositorio zonaRepositorio;

    @GetMapping("/")
    public String index(){
        return "index.html";
    }

    //Spring security añade el prefijo "ROLE_" a la hora de leer el rol necesario para acceder a la URL por lo tanto en usuario servicio se escribe con ROLE_ y aca no
    @PreAuthorize("hasAnyRole('USUARIO_REGISTRADO')")
    @GetMapping("/inicio")
    public String inicio(){

        return "inicio.html";
    }

    @GetMapping("/registro")
    public String registro(ModelMap model){
        List<Zona> zonas = zonaRepositorio.findAll();
        model.put("zonas", zonas);

        model.put("usuarioDTO", new UsuarioRegistroDTO());

        return "registro.html";
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error, @RequestParam(required = false) String logout, ModelMap model){
        if(error!=null){

            model.put("error","Usuario o clave incorrectos");
        }

        if(logout != null){
            model.put("logout", "ha salido correctamente");
        }
        return "login.html";
    }

    @PostMapping("/registrar")
    public String registrar(ModelMap model, @ModelAttribute UsuarioRegistroDTO usuarioDTO) {

        try {
            // ¡MAGIA! Le pasamos el DTO completo al servicio directamente
            usuarioServicio.registrar(usuarioDTO);

        } catch (ErrorServicio e) {
            List<Zona> zonas = zonaRepositorio.findAll();
            model.put("zonas", zonas);
            model.put("error", e.getMessage());

            // Devolvemos el DTO a la vista para que no se borren los datos que el usuario ya escribió
            model.put("usuarioDTO", usuarioDTO);

            return "registro.html";
        }

        model.put("titulo", "Bienvenido a Tinder de Mascota");
        model.put("descripcion", "Tu usuario fue creado correctamente");
        return "exito.html";
    }
}
