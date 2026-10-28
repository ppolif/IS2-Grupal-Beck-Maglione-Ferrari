package com.example.consultorio.template;


import com.example.consultorio.entities.EntidadPersona;
import com.example.consultorio.repositories.PersonaRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

//esta clase es porque el CRUD de paciente y medico es exactamente igual
//por lo tanto es buena idea usar patron plantilla, y absolutamente todos los metodos pueden implementarse
//directamente aca, porque tienen incluso los mismos atributos
//por eso hicimos EntidadPersona de la cual heredan ambas clases, nos permite usar generico en esta plantilla
//y definir todos los metodos
public abstract class TemplateCrud<T extends EntidadPersona> {

    @Autowired
    protected HttpSession session;

    @Autowired
    protected PersonaRepository<T, String> personaRepository;

    ////metodos plantillaaaaa
    //metodo plantilla de alta
    public final void ejecutarAlta(T entidad) {
        entidad.setId(null);
        validarSesion();
        validarDatos(entidad);
        verificarNoDuplicado(entidad);
        persistir(entidad);
    }

    //plantilla de baja
    public final void ejecutarBaja(String id) {
        validarSesion();
        T entidad = buscarPorId(id);

        if (entidad == null) {
            throw new RuntimeException("No encontrado en el sistema");
        }

        verificarNoEliminado(entidad);
        marcarComoEliminado(entidad);
        persistir(entidad);
    }

    //plantilla de modificacion
    public final void ejecutarModificacion(T entidadActualizada) {
        validarSesion();
        validarDatos(entidadActualizada);
        persistir(entidadActualizada);
    }


    ///metodos que son los pasos dentro de nuestros metodos plantilla
    /// en este caso no sera necesario redefinir ninguna logica en las clases hijas
    //verifica que un empleado esté logeado
    protected void validarSesion() {
        Object usuario = session.getAttribute("usuariosession");

        if (usuario == null) {
            throw new RuntimeException("Acceso denegado: Debe iniciar sesión para realizar esta operación.");
        }
    }

    protected void verificarNoEliminado(T entidad) {
        if (entidad == null) {
            return;
        }

        if (entidad.isEliminado()) {
            throw new RuntimeException("El médico o paciente ya fue eliminado");
        }
    }

    protected void validarDatos(T entidad) {
        if (entidad.getNombre() == null || entidad.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (entidad.getApellido() == null || entidad.getApellido().trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido es obligatorio.");
        }
        if (entidad.getDni() == null || entidad.getDni().trim().isEmpty()) {
            throw new IllegalArgumentException("El documento es obligatorio.");
        }
    };

    // verifica en la base de datos que no exista otro registro con el mismo dni
    protected void verificarNoDuplicado(T entidad) {
        boolean existe = personaRepository.existsByDniAndEliminadoFalse(entidad.getDni());

        if (existe) {
            throw new IllegalStateException("Ya existe un registro con el documento: " + entidad.getDni());
        }
    };

    // guarda el nuevo registro en la base de datos o en memoria
    protected void persistir(T entidad){
        personaRepository.save(entidad);
    };

    // busca un registro existente por su id
    public T buscarPorId(String id) {
        return personaRepository.findByIdAndEliminadoFalse(id).orElse(null);
    }

    // busca un registro existente por su dni
    public T buscarPorDni(String dni) {
        return personaRepository.findByDniAndEliminadoFalse(dni).orElse(null);
    }

    // cambia el estado del atributo eliminado a true
    protected void marcarComoEliminado(T entidad) {
        entidad.setEliminado(true);
        personaRepository.save(entidad);
    };

    public List<T> listarActivos() {
        return personaRepository.findByEliminadoFalse();
    }

    public abstract T instanciarNuevaEntidad();

}
