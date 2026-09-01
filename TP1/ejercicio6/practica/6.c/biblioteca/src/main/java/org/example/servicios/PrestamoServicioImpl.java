package org.example.servicios;


import org.example.dtos.LibroResponseDTO;
import org.example.dtos.PrestamoRequestDTO;
import org.example.dtos.PrestamoResponseDTO;
import org.example.dtos.UsuarioResponseDTO;
import org.example.entidades.Libro;
import org.example.entidades.Prestamo;
import org.example.entidades.Usuario;
import org.example.errores.RecursoNoEncontradoException;
import org.example.repositorios.LibroRepositorio;
import org.example.repositorios.PrestamoRepositorio;
import org.example.repositorios.UsuarioRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrestamoServicioImpl implements PrestamoServicio {

    private final PrestamoRepositorio prestamoRepositorio;
    private final LibroRepositorio libroRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;

    public PrestamoServicioImpl(PrestamoRepositorio prestamoRepositorio, LibroRepositorio libroRepositorio, UsuarioRepositorio usuarioRepositorio) {
        this.prestamoRepositorio = prestamoRepositorio;
        this.libroRepositorio = libroRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @Override
    @Transactional
    public PrestamoResponseDTO registrarPrestamo(PrestamoRequestDTO dto) {
        Libro libro = libroRepositorio.findByIsbnAndAltaTrue(dto.getIsbnLibro())
                .orElseThrow(() -> new RecursoNoEncontradoException("Libro no encontrado o inactivo"));

        Usuario usuario = usuarioRepositorio.findByIdAndAltaTrue(dto.getIdUsuario())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado o inactivo"));

        // Validación 1: Verificar stock
        if (libro.getEjemplaresRestantes() <= 0) {
            throw new IllegalStateException("No hay ejemplares disponibles para prestar de este libro.");
        }

        // Validación 2: Verificar que el usuario no tenga ya un préstamo activo de ESTE MISMO libro
        boolean yaTienePrestamo = prestamoRepositorio.findByUsuarioIdAndFechaDevolucionIsNullAndAltaTrue(usuario.getId())
                .stream()
                .anyMatch(p -> p.getLibro().getIsbn().equals(libro.getIsbn()));

        if (yaTienePrestamo) {
            throw new IllegalStateException("Ya tienes un ejemplar de este libro en tu poder. Devuélvelo antes de solicitar otro.");
        }

        // Actualizamos inventario del libro
        libro.setEjemplaresRestantes(libro.getEjemplaresRestantes() - 1);
        libro.setEjemplaresPrestados(libro.getEjemplaresPrestados() + 1);
        libroRepositorio.save(libro);

        // Creamos el préstamo
        Prestamo prestamo = Prestamo.builder()
                .fechaPrestamo(LocalDate.now())
                .alta(true)
                .libro(libro)
                .usuario(usuario)
                .build();

        prestamo = prestamoRepositorio.save(prestamo);
        return mapearADTO(prestamo);
    }

    @Override
    @Transactional
    public PrestamoResponseDTO registrarDevolucion(String idPrestamo) {
        Prestamo prestamo = prestamoRepositorio.findByIdAndAltaTrue(idPrestamo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Préstamo no encontrado"));

        if (prestamo.getFechaDevolucion() != null) {
            throw new IllegalStateException("Este préstamo ya ha sido devuelto.");
        }

        // Actualizamos inventario del libro
        Libro libro = prestamo.getLibro();
        libro.setEjemplaresRestantes(libro.getEjemplaresRestantes() + 1);
        libro.setEjemplaresPrestados(libro.getEjemplaresPrestados() - 1);
        libroRepositorio.save(libro);

        // Actualizamos el préstamo
        prestamo.setFechaDevolucion(LocalDate.now());
        prestamo = prestamoRepositorio.save(prestamo);

        return mapearADTO(prestamo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrestamoResponseDTO> obtenerTodosActivos() {
        return prestamoRepositorio.findByAltaTrue().stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrestamoResponseDTO> obtenerPrestamosActivosDeUsuario(String idUsuario) {
        return prestamoRepositorio.findByUsuarioIdAndFechaDevolucionIsNullAndAltaTrue(idUsuario).stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    // --- Métodos Privados Utilitarios ---
    private PrestamoResponseDTO mapearADTO(Prestamo prestamo) {
        // Mapeo simple de Usuario
        UsuarioResponseDTO usuarioDTO = UsuarioResponseDTO.builder()
                .id(prestamo.getUsuario().getId())
                .dni(prestamo.getUsuario().getDni())
                .nombre(prestamo.getUsuario().getNombre())
                .mail(prestamo.getUsuario().getMail())
                .rol(prestamo.getUsuario().getRol())
                .build();

        // Mapeo simple de Libro (omitiendo anidados complejos por rendimiento si no se necesitan,
        // pero respetando la estructura de tu DTO)
        LibroResponseDTO libroDTO = LibroResponseDTO.builder()
                .isbn(prestamo.getLibro().getIsbn())
                .titulo(prestamo.getLibro().getTitulo())
                .ejemplaresRestantes(prestamo.getLibro().getEjemplaresRestantes())
                .build();

        return PrestamoResponseDTO.builder()
                .id(prestamo.getId())
                .fechaPrestamo(prestamo.getFechaPrestamo())
                .fechaDevolucion(prestamo.getFechaDevolucion())
                .alta(prestamo.isAlta())
                .libro(libroDTO)
                .usuario(usuarioDTO)
                .build();
    }

}