package com.example.tinder.servicios;

import com.example.tinder.entidades.Foto;
import com.example.tinder.entidades.Usuario;
import com.example.tinder.errores.ErrorServicio;
import com.example.tinder.repositorios.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.Optional;

@Service
public class UsuarioServicio {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private FotoServicio fotoServicio;

    public void registrar(MultipartFile archivo, String nombre, String apellido, String mail, String clave) throws ErrorServicio {
        validar(nombre, apellido, mail, clave);

        Usuario usuario = new Usuario();

        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setEmail(mail);
        usuario.setClave(clave);
        usuario.setAlta(new Date());

        Foto foto = fotoServicio.guardar(archivo);
        usuario.setFoto(foto);

        usuarioRepositorio.save(usuario);

    }

    public void modificar(MultipartFile archivo, String id, String nombre, String apellido, String mail, String clave) throws ErrorServicio {

        validar(nombre, apellido, mail, clave);

        Optional<Usuario> respuesta = usuarioRepositorio.findById(id);

        if (respuesta.isPresent()) {
            Usuario usuario = respuesta.get();
            usuario.setNombre(nombre);
            usuario.setApellido(apellido);
            usuario.setEmail(mail);
            usuario.setClave(clave);

            String idFoto = null;
            if (usuario.getFoto() != null){
                idFoto = usuario.getFoto().getId();
            }

            Foto foto = fotoServicio.actualizar(idFoto, archivo);
            usuario.setFoto(foto);

            usuarioRepositorio.save(usuario);
        } else {
            throw new ErrorServicio("No se encontró el usuario");
        }
    }

    public void deshabilitar(String id) throws ErrorServicio {

        Optional<Usuario> respuesta = usuarioRepositorio.findById(id);
        if (respuesta.isPresent()) {
            Usuario usuario = respuesta.get();
            usuario.setBaja(new Date());
        } else {
            throw new ErrorServicio("No se encontró el usuario");
        }

    }

    public void habilitar(String id) throws ErrorServicio {
        Optional<Usuario> respuesta = usuarioRepositorio.findById(id);
        if (respuesta.isPresent()) {
            Usuario usuario = respuesta.get();
            usuario.setBaja(null);
        } else {
            throw new ErrorServicio("No se encontró el usuario");
        }

    }

    public void validar(String nombre, String apellido, String mail, String clave) throws ErrorServicio {
        if (nombre == null || nombre.isEmpty()) {
            throw new ErrorServicio("Debe indicar un nombre.");
        }

        if (apellido == null || apellido.isEmpty()) throw new ErrorServicio("Debe indicar un apellido.");

        if (mail == null || mail.isEmpty()) throw new ErrorServicio("Debe indicar un mail.");

        if (clave == null || clave.isEmpty() || clave.length() <= 6) {
            throw new ErrorServicio("La clave no puede ser nula, debe tener mas de 6 caracteres.");
        }

    }
}
