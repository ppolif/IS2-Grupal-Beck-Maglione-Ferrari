package com.example.tinder.servicios;

import com.example.tinder.dto.MascotaEdicionDTO;
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
    public void agregarMascota(String idUsuario, MascotaEdicionDTO dto) throws ErrorServicio {
        Usuario usuario = usuarioRepositorio.findById(idUsuario).orElseThrow();

        validar(dto.getNombre(), dto.getSexo());

        Mascota mascota = new Mascota();
        mascota.setNombre(dto.getNombre());
        mascota.setSexo(dto.getSexo());
        mascota.setTipo(dto.getTipo());
        mascota.setAlta(new Date());
        mascota.setUsuario(usuario);

        Foto foto = fotoServicio.guardar(dto.getArchivo());
        mascota.setFoto(foto);

        mascotaRepositorio.save(mascota);
    }

    @Transactional
    public void actualizar(String idUsuario, MascotaEdicionDTO dto) throws ErrorServicio {
        validar(dto.getNombre(), dto.getSexo());

        Optional<Mascota> respuesta = mascotaRepositorio.findById(dto.getId());

        if (respuesta.isPresent()) {
            Mascota mascota = respuesta.get();

            if (mascota.getUsuario().getId().equals(idUsuario)) {
                mascota.setNombre(dto.getNombre());
                mascota.setSexo(dto.getSexo());
                mascota.setTipo(dto.getTipo());

                String idFoto = null;
                if (mascota.getFoto() != null){
                    idFoto = mascota.getFoto().getId();
                }

                Foto foto = fotoServicio.actualizar(idFoto, dto.getArchivo());
                mascota.setFoto(foto);

                mascotaRepositorio.save(mascota);
            } else {
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
}
