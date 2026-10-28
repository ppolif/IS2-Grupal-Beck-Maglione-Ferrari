package com.example.persona.patterns.prototype;

import com.example.persona.entities.Autor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AutorPrototypeTest {

    @Test
    @DisplayName("clonar() debe generar una nueva instancia con sufijo ' - Copia' y sin ID")
    void testClonarAutorConSufijo() {
        Autor original = new Autor();
        original.setId(42L);
        original.setNombre("Gabriel");
        original.setApellido("García Márquez");
        original.setBiografia("Premio Nobel de Literatura 1982.");

        Autor clon = original.clonar();

        assertNotNull(clon);
        assertNotSame(original, clon, "El clon debe ser una instancia diferente en memoria");
        assertNull(clon.getId(), "El clon no debe heredar el ID para permitir nueva persistencia");
        assertEquals("Gabriel - Copia", clon.getNombre(), "Debe añadir el sufijo ' - Copia'");
        assertEquals(original.getApellido(), clon.getApellido());
        assertEquals(original.getBiografia(), clon.getBiografia());
    }

    @Test
    @DisplayName("clonarExacto() debe generar una copia fiel sin alterar el nombre")
    void testClonarExacto() {
        Autor original = new Autor();
        original.setId(10L);
        original.setNombre("Mario");
        original.setApellido("Vargas Llosa");
        original.setBiografia("Escritor peruano.");

        Autor clon = original.clonarExacto();

        assertNotNull(clon);
        assertNotSame(original, clon);
        assertEquals("Mario", clon.getNombre());
        assertEquals("Vargas Llosa", clon.getApellido());
        assertEquals("Escritor peruano.", clon.getBiografia());
    }

    @Test
    @DisplayName("Mutar el clon no debe modificar el autor original")
    void testIndependenciaDeMutacion() {
        Autor original = new Autor();
        original.setNombre("Adolfo");
        original.setApellido("Bioy Casares");
        original.setBiografia("Autor de La invención de Morel.");

        Autor clon = original.clonar();
        clon.setNombre("Nuevo Nombre");
        clon.setApellido("Nuevo Apellido");
        clon.setBiografia("Nueva biografía");

        assertEquals("Adolfo", original.getNombre());
        assertEquals("Bioy Casares", original.getApellido());
        assertEquals("Autor de La invención de Morel.", original.getBiografia());
    }
}

