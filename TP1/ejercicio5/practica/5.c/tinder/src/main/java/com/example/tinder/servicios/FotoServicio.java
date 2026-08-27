package com.example.tinder.servicios;

import com.example.tinder.entidades.Foto;
import com.example.tinder.errores.ErrorServicio;
import com.example.tinder.repositorios.FotoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
public class FotoServicio {

    @Autowired
    private FotoRepositorio fotoRepositorio;

    public Foto guardar(MultipartFile archivo) throws ErrorServicio{
        if (archivo != null && !archivo.isEmpty()) {
            try {
                Foto foto = new Foto();
                foto.setMime(archivo.getContentType()); //Devuelve el tipo del archivo mime
                foto.setNombre(archivo.getName());
                foto.setContenido(archivo.getBytes());

                return fotoRepositorio.save(foto);

            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }

        return null;
    }

    public Foto actualizar(String idFoto, MultipartFile archivo)throws ErrorServicio {
        try {
            if (archivo != null) {
                Foto foto = new Foto();

                if (idFoto != null) {

                    Optional<Foto> optional = fotoRepositorio.findById(idFoto);
                    if (optional.isPresent()) {
                        foto = optional.get();
                    }
                }

                foto.setMime(archivo.getContentType()); //Devuelve el tipo del archivo mime
                foto.setNombre(archivo.getName());
                foto.setContenido(archivo.getBytes()); //Pasa el contenido a un arreglo de bytes

                return fotoRepositorio.save(foto);

            }

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return null;
    }
}
