package com.example.tinder.controladores;


import com.example.tinder.entidades.Zona;
import com.example.tinder.errores.ErrorServicio;
import com.example.tinder.repositorios.ZonaRepositorio;
import com.example.tinder.servicios.UsuarioServicio;
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
    public String registrar(ModelMap model, MultipartFile archivo, @RequestParam String nombre, @RequestParam String apellido, @RequestParam String email, @RequestParam String clave, @RequestParam String repetirClave, @RequestParam String idZona) {

        try {
            usuarioServicio.registrar(archivo, nombre, apellido, email, clave, repetirClave, idZona);
        } catch (ErrorServicio e) {
            List<Zona> zonas = zonaRepositorio.findAll();
            model.put("zonas", zonas);

            model.put("error", e.getMessage());
            model.put("nombre", nombre);
            model.put("apellido", apellido);
            model.put("email", email);
            model.put("clave", clave);
            model.put("repetirClave", repetirClave);

            return "registro.html";
        }
        model.put("titulo", "Bienvenido a Tinder de Mascota");
        model.put("descripcion", "Tu usuario fue creado correctamente");
        return "exito.html";
    }
}
