package com.example.tinder.servicios;

import com.example.tinder.entidades.Foto;
import com.example.tinder.entidades.Usuario;
import com.example.tinder.entidades.Zona;
import com.example.tinder.enumeraciones.Rol;
import com.example.tinder.errores.ErrorServicio;
import com.example.tinder.repositorios.UsuarioRepositorio;
import com.example.tinder.repositorios.ZonaRepositorio;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UsuarioServicio implements UserDetailsService {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private FotoServicio fotoServicio;

    @Autowired
    private ZonaRepositorio zonaRepositorio;

    @Autowired
    private NotificacionServicio notificacionServicio;

    /**
     * NUEVO (Ejercicio 5.e): Este metodo lo creamos para conectar el login de Auth0 con nuestra base de datos local.
     * Como el resto de la aplicacion (mascotas, editar perfil, etc.) necesita un objeto Usuario en la tabla de MySQL,
     * cuando alguien entra con Auth0 buscamos si su mail ya existe en la BD. Si ya existe lo devolvemos,
     * y si es la primera vez que entra con Auth0, le creamos su registro de Usuario con Rol.USER automaticamente.
     */
    @Transactional
    public Usuario obtenerOCrearUsuarioOAuth2(String email, String nombreCompleto, String givenName, String familyName) {
        if (email == null || email.isBlank()) {
            return null;
        }
        Usuario usuario = usuarioRepositorio.buscarPorMail(email);
        if (usuario != null) {
            return usuario;
        }

        // Si no existia en nuestra BD, lo instanciamos con los datos que nos trajo el perfil de Auth0
        usuario = new Usuario();
        String nombre = (givenName != null && !givenName.isBlank()) ? givenName
                : (nombreCompleto != null && !nombreCompleto.isBlank() ? nombreCompleto : email.split("@")[0]);
        String apellido = (familyName != null && !familyName.isBlank()) ? familyName : "";

        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setEmail(email);
        // Le generamos una clave aleatoria encriptada porque la autenticacion real la maneja Auth0
        usuario.setClave(new BCryptPasswordEncoder().encode(UUID.randomUUID().toString()));
        usuario.setAlta(new Date());
        usuario.setRol(Rol.USER);

        // Si hay zonas cargadas en la BD, le asignamos una por defecto (despues la puede cambiar en Editar Perfil)
        List<Zona> zonas = zonaRepositorio.findAll();
        if (!zonas.isEmpty()) {
            usuario.setZona(zonas.get(0));
        }

        return usuarioRepositorio.save(usuario);
    }


    @Transactional
    public void registrar(MultipartFile archivo, String nombre, String apellido, String mail, String clave, String repetirClave, String idZona) throws ErrorServicio {
        Zona zona = zonaRepositorio.getOne(idZona);

        validar(nombre, apellido, mail, clave, repetirClave, zona);

        Usuario usuario = new Usuario();

        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setEmail(mail);
        usuario.setZona(zona);
        String claveEncriptada = new BCryptPasswordEncoder().encode(clave);
        usuario.setClave(claveEncriptada);
        usuario.setAlta(new Date());

        //esto es nuevo, le asignamos rol
        usuario.setRol(Rol.USER);

        Foto foto = fotoServicio.guardar(archivo);
        usuario.setFoto(foto);

        usuarioRepositorio.save(usuario);

        //notificacionServicio.enviar("Bienvenido", "Tinder de Mascotas", usuario.getEmail());
    }

    @Transactional
    public void modificar(MultipartFile archivo, String id, String nombre, String apellido, String mail, String clave, String repetirClave, String idZona) throws ErrorServicio {
        Zona zona = zonaRepositorio.getOne(idZona);

        validar(nombre, apellido, mail, clave, repetirClave, zona);

        Optional<Usuario> respuesta = usuarioRepositorio.findById(id);

        if (respuesta.isPresent()) {
            Usuario usuario = respuesta.get();
            usuario.setNombre(nombre);
            usuario.setApellido(apellido);
            usuario.setEmail(mail);
            usuario.setZona(zona);

            //encriptacion
            String claveEncriptada = new BCryptPasswordEncoder().encode(clave);
            usuario.setClave(claveEncriptada);

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

    @Transactional
    public void deshabilitar(String id) throws ErrorServicio {

        Optional<Usuario> respuesta = usuarioRepositorio.findById(id);
        if (respuesta.isPresent()) {
            Usuario usuario = respuesta.get();
            usuario.setBaja(new Date());
        } else {
            throw new ErrorServicio("No se encontró el usuario");
        }

    }

    @Transactional
    public void habilitar(String id) throws ErrorServicio {
        Optional<Usuario> respuesta = usuarioRepositorio.findById(id);
        if (respuesta.isPresent()) {
            Usuario usuario = respuesta.get();
            usuario.setBaja(null);
        } else {
            throw new ErrorServicio("No se encontró el usuario");
        }

    }

    public void validar(String nombre, String apellido, String mail, String clave, String repetirClave, Zona zona) throws ErrorServicio {
        if (nombre == null || nombre.isEmpty()) {
            throw new ErrorServicio("Debe indicar un nombre.");
        }

        if (apellido == null || apellido.isEmpty()) throw new ErrorServicio("Debe indicar un apellido.");

        if (mail == null || mail.isEmpty()) throw new ErrorServicio("Debe indicar un mail.");

        if (clave == null || clave.isEmpty() || clave.length() <= 6) {
            throw new ErrorServicio("La clave no puede ser nula, debe tener mas de 6 caracteres.");
        }

        if (!clave.equals(repetirClave)) {
            throw new ErrorServicio("Las claves deben ser iguales");
        }

        if (zona == null) {
            throw new ErrorServicio("No se encontró la zona solicitada");
        }

    }

    @Transactional
    public Usuario buscarPorId(String id) throws ErrorServicio {
        Optional<Usuario> respuesta = usuarioRepositorio.findById(id);

        if (respuesta.isPresent()) {
            return respuesta.get();
        } else {
            throw new ErrorServicio("No se encontró el usuario");
        }
    }

    //este metodo lo llama spring automaticamente cuando el user envia el
    //formulario de inicio de sesion
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepositorio.buscarPorMail(email);
        if (usuario != null) {
            List<GrantedAuthority> permisos = new ArrayList<>();

            //toda esta logica ya estaba desde el ejercicio 5, pero cambia el rol
            //por el que definimos en la enumeracion
            GrantedAuthority p1 = new SimpleGrantedAuthority("ROLE_" + usuario.getRol().toString());
            permisos.add(p1);

            // Guarda la sesión únicamente si existe una petición HTTP activa con sesión
            try {
                ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attr != null && attr.getRequest() != null) {
                    HttpSession session = attr.getRequest().getSession(false);
                    if (session != null) {
                        session.setAttribute("usuariosession", usuario);
                    }
                }
            } catch (Exception ignored) {
            }

            return new User(usuario.getEmail(), usuario.getClave(), permisos);
        } else {
            throw new UsernameNotFoundException("Usuario no encontrado con mail: " + email);
        }
    }
}
