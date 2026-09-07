package com.example.tinder.servicios;

import com.example.tinder.entidades.Mascota;
import com.example.tinder.entidades.Voto;
import com.example.tinder.errores.ErrorServicio;
import com.example.tinder.repositorios.MascotaRepositorio;
import com.example.tinder.repositorios.VotoRepositorio;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class VotoServicio {

    @Autowired
    private VotoRepositorio votoRepositorio;

    @Autowired
    private MascotaRepositorio mascotaRepositorio;

    @Autowired
    private NotificacionServicio notificacionServicio;


    @Transactional
    public void votar(String idUsuario, String idMascota1, String idMascota2) throws ErrorServicio{
        Voto voto = new Voto();
        voto.setFecha(new Date());

        if (idMascota1.equals(idMascota2)) {
            throw new ErrorServicio("No se puede votar a si mismo");
        }

        Optional<Mascota> respuesta = mascotaRepositorio.findById(idMascota1);
        if (respuesta.isPresent()) {
            Mascota mascota1 = respuesta.get();

            if (mascota1.getUsuario().getId().equals(idUsuario)){
                voto.setMascota1(mascota1);
            }else {
                throw new ErrorServicio("No tiene permisos para realizar la operacion solicitada");
            }

        } else {
            throw new ErrorServicio("No existe la mascota");
        }

        Optional<Mascota> respuesta2 = mascotaRepositorio.findById(idMascota2);

        if (respuesta2.isPresent()) {
            Mascota mascota2 = respuesta.get();

            notificacionServicio.enviar("Tu mascota ha sido votada", "Tinder de Mascota", mascota2.getUsuario().getEmail());

            voto.setMascota2(mascota2);

        } else {
            throw new ErrorServicio("No existe la mascota");
        }
        votoRepositorio.save(voto);

    }

    @Transactional
    public void responder(String idUsuario, String idVoto) throws ErrorServicio {
        Optional<Voto> respuesta = votoRepositorio.findById(idVoto);

        if (respuesta.isPresent()) {
            Voto voto = respuesta.get();
            voto.setRespuesta(new Date());

            if (voto.getMascota2().getUsuario().getId().equals(idUsuario)) {
                notificacionServicio.enviar("Tu voto fue correspondido", "Tinder de Mascota", voto.getMascota2().getUsuario().getEmail());

                votoRepositorio.save(voto);
            } else {
                throw new ErrorServicio("No tiene permisos para realizar esta accion");
            }
        } else {
            throw new ErrorServicio("No existe el voto");
        }
    }
}
