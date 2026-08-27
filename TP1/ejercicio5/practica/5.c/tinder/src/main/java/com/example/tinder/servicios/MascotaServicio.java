package com.example.tinder.servicios;

import com.example.tinder.entidades.Foto;
import com.example.tinder.entidades.Mascota;
import com.example.tinder.entidades.Usuario;
import com.example.tinder.enumeraciones.Sexo;
import com.example.tinder.errores.ErrorServicio;
import com.example.tinder.repositorios.MascotaRepositorio;
import com.example.tinder.repositorios.UsuarioRepositorio;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.Optional;

@Service
public class MascotaServicio {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private MascotaRepositorio mascotaRepositorio;

    @Autowired
    private FotoServicio fotoServicio;

    @Transactional
    public void agregarMascota(MultipartFile archivo, String idUsuario, String nombre, Sexo sexo) throws ErrorServicio{

        Usuario usuario = usuarioRepositorio.findById(idUsuario).get();

        validar(nombre, sexo);

        Mascota mascota = new Mascota();
        mascota.setNombre(nombre);
        mascota.setSexo(sexo);
        mascota.setAlta(new Date());
        mascota.setUsuario(usuario);

        Foto foto = fotoServicio.guardar(archivo);
        mascota.setFoto(foto);

        mascotaRepositorio.save(mascota);


    }

    @Transactional
    public void modificar(MultipartFile archivo, String idUsuario, String idMascota, String nombre, Sexo sexo) throws ErrorServicio {
        validar(nombre, sexo);

        Optional<Mascota> respuesta = mascotaRepositorio.findById(idMascota);

        if (respuesta.isPresent()) {
            Mascota mascota = respuesta.get();

            if (mascota.getUsuario().getId().equals(idUsuario)) {
                mascota.setNombre(nombre);
                mascota.setSexo(sexo);

                String idFoto = null;
                if (mascota.getFoto() != null){
                    idFoto = mascota.getFoto().getId();
                }

                Foto foto = fotoServicio.actualizar(idFoto, archivo);
                mascota.setFoto(foto);

                mascotaRepositorio.save(mascota);
            }else {
                throw new ErrorServicio("El usuario debe ser el dueño de la mascota");

            }

        } else {
            throw new ErrorServicio("No existe la mascota");
        }
    }

    @Transactional
    public void eliminarMascota(String idUsuario, String idMascota)throws ErrorServicio {
        Optional<Mascota> optional = mascotaRepositorio.findById(idMascota);

        if (optional.isPresent()){
            Mascota mascota = optional.get();
            if (mascota.getUsuario().getId().equals(idUsuario)) {
                mascota.setBaja(new Date());
                mascotaRepositorio.save(mascota);
            }else{
                throw new ErrorServicio("El usuario debe ser el dueño de la mascota");
            }
        }else{
            throw new ErrorServicio("Debe indicar una mascota");
        }

    }


    private void validar(String nombre, Sexo sexo) throws ErrorServicio {

        if (nombre == null || nombre.isEmpty()) {
            throw new ErrorServicio("Ingrese el nombre de la mascota");
        }

        if (sexo == null) {
            throw new ErrorServicio("Ingrese el sexo de la mascota");
        }
    }
}
