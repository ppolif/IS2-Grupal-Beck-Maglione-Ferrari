package com.example.tinder.servicios;

import com.example.tinder.entidades.Foto;
import com.example.tinder.entidades.Mascota;
import com.example.tinder.entidades.Usuario;
import com.example.tinder.enumeraciones.Sexo;
import com.example.tinder.enumeraciones.Tipo;
import com.example.tinder.errores.ErrorServicio;
import com.example.tinder.repositorios.MascotaRepositorio;
import com.example.tinder.repositorios.UsuarioRepositorio;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
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
    public Mascota agregarMascota(MultipartFile archivo, String idUsuario, String nombre, Sexo sexo, Tipo tipo) throws ErrorServicio {
        Optional<Usuario> usuarioOpt = usuarioRepositorio.findById(idUsuario);
        if (!usuarioOpt.isPresent()) {
            throw new ErrorServicio("No se encontró el usuario indicado");
        }
        Usuario usuario = usuarioOpt.get();

        validar(nombre, sexo);

        Mascota mascota = new Mascota();
        mascota.setNombre(nombre);
        mascota.setSexo(sexo);
        mascota.setAlta(new Date());
        mascota.setUsuario(usuario);
        mascota.setTipo(tipo);

        if (archivo != null && !archivo.isEmpty()) {
            Foto foto = fotoServicio.guardar(archivo);
            mascota.setFoto(foto);
        }

        return mascotaRepositorio.save(mascota);
    }

    @Transactional
    public Mascota actualizar(MultipartFile archivo, String idUsuario, String idMascota, String nombre, Sexo sexo, Tipo tipo) throws ErrorServicio {
        validar(nombre, sexo);

        Optional<Mascota> respuesta = mascotaRepositorio.findById(idMascota);

        if (respuesta.isPresent()) {
            Mascota mascota = respuesta.get();

            if (idUsuario != null && !idUsuario.isEmpty() && !mascota.getUsuario().getId().equals(idUsuario)) {
                throw new ErrorServicio("El usuario debe ser el dueño de la mascota");
            }

            mascota.setNombre(nombre);
            mascota.setSexo(sexo);
            mascota.setTipo(tipo);

            if (archivo != null && !archivo.isEmpty()) {
                String idFoto = null;
                if (mascota.getFoto() != null) {
                    idFoto = mascota.getFoto().getId();
                }

                Foto foto = fotoServicio.actualizar(idFoto, archivo);
                mascota.setFoto(foto);
            }

            return mascotaRepositorio.save(mascota);
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

    @Transactional
    public Mascota buscarPorId(String id) throws ErrorServicio {
        Optional<Mascota> respuesta = mascotaRepositorio.findById(id);

        if (respuesta.isPresent()) {
            return respuesta.get();
        } else {
            throw new ErrorServicio("La mascota solicitada no existe. ");
        }
    }

    public List<Mascota> buscarMascotasPorUsuario(String id) {
        return mascotaRepositorio.buscarMascotaPorUsuario(id);
    }

    public List<Mascota> listarTodas() {
        return mascotaRepositorio.findAll();
    }

    @Transactional
    public Mascota actualizarFoto(String idMascota, MultipartFile archivo) throws ErrorServicio {
        Mascota mascota = buscarPorId(idMascota);
        String idFoto = mascota.getFoto() != null ? mascota.getFoto().getId() : null;
        Foto foto = fotoServicio.actualizar(idFoto, archivo);
        mascota.setFoto(foto);
        return mascotaRepositorio.save(mascota);
    }
}
