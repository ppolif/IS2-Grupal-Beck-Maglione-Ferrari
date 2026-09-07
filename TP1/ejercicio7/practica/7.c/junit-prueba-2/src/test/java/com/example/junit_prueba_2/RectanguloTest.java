package com.example.junit_prueba_2;

import com.example.junit_prueba_2.entities.Rectangulo;
import com.example.junit_prueba_2.services.RectanguloService;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RectanguloTest {

    RectanguloService rectanguloService;

    @BeforeEach
    public void setUp() {
        rectanguloService = new RectanguloService();
    }

    @Test
    public void deberiaInicializarConColor() {
        assertNotNull(new Rectangulo(10, 10).getColor());
    }

    @Test
    public void deberiaCalcularArea() {
        assertEquals(100, rectanguloService.calcularArea(new Rectangulo(10, 10)), 0);
        assertEquals(20, rectanguloService.calcularArea(new Rectangulo(4, 5)), 0);
        assertEquals(1, rectanguloService.calcularArea(new Rectangulo(1, 1)), 0);
    }

    @Test
    public void deberiaCalcularPerimetro() {
        assertEquals(40, rectanguloService.calcularPerimetro(new Rectangulo(10, 10)), 0);
        assertEquals(100, rectanguloService.calcularPerimetro(new Rectangulo(20, 30)), 0);
        assertEquals(30, rectanguloService.calcularPerimetro(new Rectangulo(5, 10)), 0);
    }

    @Test
    public void deberiaActivarODesactivar() {
        Rectangulo r = new Rectangulo(5, 5);
        assertTrue(r.isActivo());
        r.setActivo(false);
        assertFalse(r.isActivo());
    }
}
