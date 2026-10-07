package com.example.persona.services;

public interface ReporteService {
    byte[] generarPdfPersonasConPrestamos() throws Exception;
    byte[] generarExcelLibrosDisponibles() throws Exception;
}

