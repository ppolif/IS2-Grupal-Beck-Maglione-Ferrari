package com.example.tinder.controladores;

import com.example.tinder.entidades.Mascota;
import com.example.tinder.entidades.Usuario;
import com.example.tinder.errores.ErrorServicio;
import com.example.tinder.servicios.MascotaServicio;
import com.example.tinder.servicios.UsuarioServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/foto")
public class FotoController {

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private MascotaServicio mascotaServicio;


    @GetMapping("/usuario/(id)")
    public ResponseEntity<byte[]> fotoUsuario(@PathVariable String id){
        try {

            Usuario usuario = usuarioServicio.buscarPorId(id);
            if (usuario.getFoto() == null){
                throw new ErrorServicio("El usuario no tiene una foto asignada. ");
            }
            byte[] foto = usuario.getFoto().getContenido();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);

            return new ResponseEntity<>(foto, headers, HttpStatus.OK);
        } catch (ErrorServicio e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/mascota/(id)")
    public ResponseEntity<byte[]> fotoMascota(@PathVariable String id){
        try {

            Mascota mascota = mascotaServicio.buscarPorId(id);
            if (mascota.getFoto() == null){
                throw new ErrorServicio("La mascota no tiene una foto asignada. ");
            }
            byte[] foto = mascota.getFoto().getContenido();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);

            return new ResponseEntity<>(foto, headers, HttpStatus.OK);
        } catch (ErrorServicio e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


}
