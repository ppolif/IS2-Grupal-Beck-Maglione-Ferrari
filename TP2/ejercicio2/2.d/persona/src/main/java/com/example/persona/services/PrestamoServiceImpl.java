package com.example.persona.services;

import com.example.persona.dtos.AltaPrestamoDto;
import com.example.persona.dtos.PrestamoDto;
import com.example.persona.entities.Libro;
import com.example.persona.entities.Persona;
import com.example.persona.entities.Prestamo;
import com.example.persona.repositories.BaseRepository;
import com.example.persona.repositories.LibroRepository;
import com.example.persona.repositories.PersonaRepository;
import com.example.persona.repositories.PrestamoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PrestamoServiceImpl extends BaseServiceImpl<Prestamo, PrestamoDto, Long> implements PrestamoService {

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private LibroRepository libroRepository;

    public PrestamoServiceImpl(BaseRepository<Prestamo, Long> baseRepository) {
        super(baseRepository, Prestamo.class, PrestamoDto.class);
    }

    @Override
    @Transactional
    public PrestamoDto darDeAlta(AltaPrestamoDto altaDto) throws Exception {
        if (altaDto == null || altaDto.getPersonaId() == null || altaDto.getLibroId() == null) {
            throw new Exception("Los identificadores de persona y libro son obligatorios.");
        }

        Persona persona = personaRepository.findById(altaDto.getPersonaId())
                .orElseThrow(() -> new Exception("Persona no encontrada con ID: " + altaDto.getPersonaId()));

        if (Boolean.FALSE.equals(persona.getActivo())) {
            throw new Exception("La persona seleccionada se encuentra dada de baja.");
        }

        Libro libro = libroRepository.findById(altaDto.getLibroId())
                .orElseThrow(() -> new Exception("Libro no encontrado con ID: " + altaDto.getLibroId()));

        if (Boolean.FALSE.equals(libro.getActivo())) {
            throw new Exception("El libro seleccionado se encuentra dado de baja.");
        }

        if (Boolean.TRUE.equals(libro.getPrestado())) {
            throw new Exception("El libro '" + libro.getTitulo() + "' ya se encuentra prestado y no está disponible.");
        }

        // Marcar el libro como prestado
        libro.setPrestado(true);
        libroRepository.save(libro);

        // Actualizar la persona con el préstamo activo
        persona.setTienePrestamo(true);
        personaRepository.save(persona);

        // Crear registro de préstamo
        Prestamo prestamo = new Prestamo();
        prestamo.setPersona(persona);
        prestamo.setLibro(libro);
        prestamo.setFechaPrestamo(altaDto.getFechaPrestamo() != null ? altaDto.getFechaPrestamo() : LocalDate.now());
        prestamo.setEstado("ACTIVO");
        prestamo.setActivo(true);

        prestamo = baseRepository.save(prestamo);
        return toDto(prestamo);
    }

    @Override
    @Transactional
    public PrestamoDto devolver(Long prestamoId) throws Exception {
        Prestamo prestamo = baseRepository.findById(prestamoId)
                .orElseThrow(() -> new Exception("Préstamo no encontrado con ID: " + prestamoId));

        if ("DEVUELTO".equalsIgnoreCase(prestamo.getEstado())) {
            throw new Exception("El préstamo ya fue devuelto previamente.");
        }

        // Modificar el registro del préstamo asignando el estado DEVUELTO y fecha de devolución
        prestamo.setEstado("DEVUELTO");
        prestamo.setFechaDevolucion(LocalDate.now());

        // Liberar el libro para que vuelva a estar disponible
        Libro libro = prestamo.getLibro();
        if (libro != null) {
            libro.setPrestado(false);
            libroRepository.save(libro);
        }

        // Actualizar el estado de la persona si no le quedan otros préstamos activos
        Persona persona = prestamo.getPersona();
        if (persona != null) {
            boolean tieneOtrosActivos = prestamoRepository.existsByPersonaIdAndEstadoAndIdNot(persona.getId(), "ACTIVO", prestamo.getId());
            persona.setTienePrestamo(tieneOtrosActivos);
            personaRepository.save(persona);
        }

        prestamo = baseRepository.save(prestamo);
        return toDto(prestamo);
    }

    @Override
    @Transactional
    public List<PrestamoDto> findActivos() throws Exception {
        try {
            List<Prestamo> prestamos = prestamoRepository.findByEstado("ACTIVO");
            return prestamos.stream()
                    .filter(p -> p.getActivo() == null || Boolean.TRUE.equals(p.getActivo()))
                    .map(this::toDto)
                    .toList();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean delete(Long id) throws Exception {
        try {
            Optional<Prestamo> prestamoOpt = baseRepository.findById(id);
            if (prestamoOpt.isEmpty() || Boolean.FALSE.equals(prestamoOpt.get().getActivo())) {
                throw new Exception("Préstamo no encontrado con ID: " + id);
            }
            Prestamo prestamo = prestamoOpt.get();
            prestamo.setActivo(false); // Eliminado lógico

            // Si el préstamo estaba ACTIVO al eliminarse lógicamente, se libera el libro
            if ("ACTIVO".equalsIgnoreCase(prestamo.getEstado())) {
                prestamo.setEstado("DEVUELTO");
                if (prestamo.getFechaDevolucion() == null) {
                    prestamo.setFechaDevolucion(LocalDate.now());
                }
                if (prestamo.getLibro() != null) {
                    prestamo.getLibro().setPrestado(false);
                    libroRepository.save(prestamo.getLibro());
                }
                if (prestamo.getPersona() != null) {
                    boolean tieneOtrosActivos = prestamoRepository.existsByPersonaIdAndEstadoAndIdNot(prestamo.getPersona().getId(), "ACTIVO", prestamo.getId());
                    prestamo.getPersona().setTienePrestamo(tieneOtrosActivos);
                    personaRepository.save(prestamo.getPersona());
                }
            }

            baseRepository.save(prestamo);
            return true;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}
