package com.example.persona.services;

import java.time.LocalDate;

public interface EmailService {
    void enviarAvisoVencimientoLibro(String destinatario, String nombrePersona, String tituloLibro, LocalDate fechaVencimiento);
    void enviarSaludoCumpleanios(String destinatario, String nombrePersona);
}
