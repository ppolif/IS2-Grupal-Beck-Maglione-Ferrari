package com.example.persona.services;

import com.example.persona.entities.Libro;
import com.example.persona.entities.Persona;
import com.example.persona.repositories.PersonaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class NotificacionServiceImpl implements NotificacionService {

    private final PersonaRepository personaRepository;
    private final EmailService emailService;

    public NotificacionServiceImpl(PersonaRepository personaRepository, EmailService emailService) {
        this.personaRepository = personaRepository;
        this.emailService = emailService;
    }

    @Override
    @Transactional(readOnly = true)
    public void enviarAvisosVencimientoLibros() {
        LocalDate hoy = LocalDate.now();
        LocalDate maniana = hoy.plusDays(1);

        List<Persona> personas = personaRepository.findAllConLibros();

        for (Persona persona : personas) {
            if (persona.getEmail() == null || persona.getEmail().isBlank()) {
                continue;
            }
            if (persona.getLibros() != null) {
                for (Libro libro : persona.getLibros()) {
                    if (libro.getFechaVencimientoDevolucion() != null 
                            && libro.getFechaVencimientoDevolucion().isEqual(maniana)) {
                        String nombreCompleto = persona.getNombre() + " " + persona.getApellido();
                        emailService.enviarAvisoVencimientoLibro(
                                persona.getEmail(),
                                nombreCompleto,
                                libro.getTitulo(),
                                libro.getFechaVencimientoDevolucion()
                        );
                    }
                }
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void enviarSaludosCumpleanios() {
        LocalDate hoy = LocalDate.now();

        List<Persona> personas = personaRepository.findAll();

        for (Persona persona : personas) {
            if (persona.getEmail() == null || persona.getEmail().isBlank() || persona.getFechaNacimiento() == null) {
                continue;
            }

            LocalDate fechaNac = persona.getFechaNacimiento();
            boolean esCumpleanios = fechaNac.getMonthValue() == hoy.getMonthValue()
                    && fechaNac.getDayOfMonth() == hoy.getDayOfMonth();

            // Manejo de personas nacidas el 29 de febrero en años no bisiestos
            if (!esCumpleanios && !hoy.isLeapYear() && hoy.getMonthValue() == 2 && hoy.getDayOfMonth() == 28
                    && fechaNac.getMonthValue() == 2 && fechaNac.getDayOfMonth() == 29) {
                esCumpleanios = true;
            }

            if (esCumpleanios) {
                String nombreCompleto = persona.getNombre() + " " + persona.getApellido();
                emailService.enviarSaludoCumpleanios(persona.getEmail(), nombreCompleto);
            }
        }
    }
}
