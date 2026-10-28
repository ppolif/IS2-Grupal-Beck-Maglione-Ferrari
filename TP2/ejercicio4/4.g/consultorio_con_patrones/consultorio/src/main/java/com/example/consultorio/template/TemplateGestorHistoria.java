package com.example.consultorio.template;

import com.example.consultorio.entities.DetalleHistoriaClinica;
import com.example.consultorio.entities.HistoriaClinica;
import com.example.consultorio.entities.Paciente;
import com.example.consultorio.entities.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

//esta plantilla es para la gestion de historias clinicas
//es util tener metodos plantilla porque hay que seguir pasos determinados que involucran
//operaciones tanto con HistoriaClinica como con DetalleHistoriaClinica
public abstract class TemplateGestorHistoria {
    @Autowired
    protected HttpSession session;

    ///metodos plantilla
    //metodo plantilla crear
    public final void ejecutarRegistro(String idPaciente, DetalleHistoriaClinica primerDetalle) {
        Usuario usuario = validarSesion();

        Paciente paciente = obtenerPacienteActivo(idPaciente);

        verificarHistoriaInexistente(paciente);

        HistoriaClinica nuevaHistoria = inicializarHistoria(paciente, usuario);
        asociarDetalleAHistoria(nuevaHistoria, primerDetalle);

        persistirHistoria(nuevaHistoria);
    }

    //metodo plantilla modificar
    public final void ejecutarAgregarDetalle(String idPaciente, DetalleHistoriaClinica nuevoDetalle) {
        validarSesion();
        Paciente paciente = obtenerPacienteActivo(idPaciente);
        HistoriaClinica historia = obtenerHistoriaDelPaciente(paciente);
        verificarHistoriaActiva(historia);
        validarDetalle(nuevoDetalle);
        asociarDetalleAHistoria(historia, nuevoDetalle);
        persistirHistoria(historia);
    }

    //metodo plantilla para baja de detalles
    //nosotros lo implementamos como que una vez creada la historia medica, lo unico que cambian son los detalles
    //y a su vez los detalles una vez creados no se modifican, si hay que hacer un cambio hay que eliminarlo
    //y volver a hacerlo en el momento, para asegurar que no se modifique información de turnos pasados
    public final void ejecutarBajaDetalle(String idHistoria, String idDetalle) {
        validarSesion();
        HistoriaClinica historia = obtenerHistoriaPorId(idHistoria);
        verificarHistoriaActiva(historia);
        marcarDetalleEliminado(historia, idDetalle);
        persistirHistoria(historia);
    }


    ///metodos cuya no logica no necesita ser redefinida en las clases hijas
    protected Usuario validarSesion() {
        Usuario usuario = (Usuario) session.getAttribute("usuariosession");

        if (usuario == null) {
            throw new RuntimeException("Acceso denegado: Debe iniciar sesión para realizar esta operación.");
        }

        return usuario;
    }

    protected void verificarHistoriaActiva(HistoriaClinica historia) {
        if (historia == null || historia.isEliminado()) {
            throw new RuntimeException("La historia clínica solicitada no existe o fue dada de baja.");
        }
    }

    protected void validarDetalle(DetalleHistoriaClinica detalle) {
        if (detalle == null) {
            throw new IllegalArgumentException("El detalle médico no puede ser nulo.");
        }
        if (detalle.getDetalleHistoria() == null || detalle.getDetalleHistoria().trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción del detalle médico es obligatoria.");
        }
        if (detalle.getMedico() == null) {
            throw new IllegalArgumentException("Debe asignar un médico al detalle de la historia clínica.");
        }
    }

    ///metodos a sobre escribir en las clases hija
    protected abstract Paciente obtenerPacienteActivo(String idPaciente);
    protected abstract void verificarHistoriaInexistente(Paciente paciente);
    protected abstract HistoriaClinica inicializarHistoria(Paciente paciente, Usuario usuario);
    protected abstract void asociarDetalleAHistoria(HistoriaClinica historia, DetalleHistoriaClinica detalle);
    protected abstract void persistirHistoria(HistoriaClinica historia);
    protected abstract void marcarDetalleEliminado(HistoriaClinica historia, String idDetalle);
    protected abstract HistoriaClinica obtenerHistoriaDelPaciente(Paciente paciente);
    protected abstract HistoriaClinica obtenerHistoriaPorId(String idHistoria);

}

