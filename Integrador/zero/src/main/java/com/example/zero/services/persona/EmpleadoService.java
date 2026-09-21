package com.example.zero.services.persona;

import com.example.zero.entidades.empresa.Empresa;
import com.example.zero.entidades.persona.Empleado;
import com.example.zero.entidades.persona.Usuario;
import com.example.zero.enums.TipoDocumento;
import com.example.zero.enums.TipoEmpleado;
import com.example.zero.repositories.EmpleadoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;

    public EmpleadoService(EmpleadoRepository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }

    public void validar(String numeroDocumento, String nombre, String apellido) {
        if (numeroDocumento == null || numeroDocumento.trim().isEmpty()) {
            throw new IllegalArgumentException("El número de documento no puede estar vacío");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        if (apellido == null || apellido.trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido no puede estar vacío");
        }
    }

    @Transactional
    public Empleado crearEmpleado(String numeroDocumento, String nombre, String apellido,
                                  LocalDate fechaNacimiento, TipoDocumento tipoDocumento,
                                  TipoEmpleado tipoEmpleado, Empresa empresa) {
        validar(numeroDocumento, nombre, apellido);
        String docLimpio = numeroDocumento.trim();

        Optional<Empleado> existente = empleadoRepository.findByNumeroDocumentoAndEliminadoFalse(docLimpio);
        if (existente.isPresent()) {
            throw new IllegalArgumentException("Ya existe un empleado activo con el documento: " + docLimpio);
        }

        Empleado empleado = Empleado.builder()
                .numeroDocumento(docLimpio)
                .nombre(nombre.trim())
                .apellido(apellido.trim())
                .fechaNacimiento(fechaNacimiento != null ? fechaNacimiento : LocalDate.of(1990, 1, 1))
                .tipoDocumento(tipoDocumento != null ? tipoDocumento : TipoDocumento.DNI)
                .tipoEmpleado(tipoEmpleado != null ? tipoEmpleado : TipoEmpleado.ADMINISTRATIVO)
                .empresa(empresa)
                .eliminado(false)
                .build();

        return empleadoRepository.save(empleado);
    }

    @Transactional
    public Empleado modificarEmpleado(String numeroDocumento, String nombre, String apellido,
                                      LocalDate fechaNacimiento, TipoDocumento tipoDocumento,
                                      TipoEmpleado tipoEmpleado, Empresa empresa) {
        Empleado empleado = buscarPorDocumento(numeroDocumento);

        if (nombre != null && !nombre.trim().isEmpty()) {
            empleado.setNombre(nombre.trim());
        }
        if (apellido != null && !apellido.trim().isEmpty()) {
            empleado.setApellido(apellido.trim());
        }
        if (fechaNacimiento != null) {
            empleado.setFechaNacimiento(fechaNacimiento);
        }
        if (tipoDocumento != null) {
            empleado.setTipoDocumento(tipoDocumento);
        }
        if (tipoEmpleado != null) {
            empleado.setTipoEmpleado(tipoEmpleado);
        }
        if (empresa != null) {
            empleado.setEmpresa(empresa);
        }

        return empleadoRepository.save(empleado);
    }

    @Transactional
    public void eliminarEmpleado(String numeroDocumento) {
        Empleado empleado = buscarPorDocumento(numeroDocumento);
        empleado.setEliminado(true);
        empleadoRepository.save(empleado);
    }

    @Transactional(readOnly = true)
    public Empleado buscarPorDocumento(String numeroDocumento) {
        if (numeroDocumento == null || numeroDocumento.trim().isEmpty()) {
            throw new IllegalArgumentException("El número de documento no puede ser nulo o vacío");
        }
        return empleadoRepository.findByNumeroDocumentoAndEliminadoFalse(numeroDocumento.trim())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el empleado con documento: " + numeroDocumento));
    }

    @Transactional
    public Empleado asociarEmpleadoUsuario(String numeroDocumento, Usuario usuario) {
        Empleado empleado = buscarPorDocumento(numeroDocumento);
        empleado.setUsuario(usuario);
        return empleadoRepository.save(empleado);
    }

    @Transactional(readOnly = true)
    public List<Empleado> listarActivos() {
        return empleadoRepository.findByEliminadoFalse();
    }

    @Transactional(readOnly = true)
    public List<Empleado> listarTodos() {
        return empleadoRepository.findAll();
    }
}
