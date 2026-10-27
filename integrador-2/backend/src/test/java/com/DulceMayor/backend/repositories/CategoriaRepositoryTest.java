package com.DulceMayor.backend.repositories;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.DulceMayor.backend.entities.Categoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests de repositorio para {@link CategoriaRepository} sobre H2 en memoria (perfil test).
 * Verifica las consultas de duplicados sin distinguir mayúsculas/minúsculas y de exclusión por propio ID.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CategoriaRepositoryTest {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @BeforeEach
    void limpiar() {
        categoriaRepository.deleteAll();
    }

    @Test
    @DisplayName("Consulta de duplicados detecta nombres iguales sin distinguir mayúsculas ni minúsculas")
    void consultaDuplicadosSinDistinguirMayusculas() {
        Categoria guardada = categoriaRepository.save(Categoria.builder()
                .nombre("Alfajores")
                .eliminado(false)
                .build());

        assertTrue(categoriaRepository.existsByNombreIgnoreCase("alfajores"));
        assertTrue(categoriaRepository.existsByNombreIgnoreCase("ALFAJORES"));
        assertTrue(categoriaRepository.existsByNombreIgnoreCase("AlFaJoReS"));
        assertTrue(categoriaRepository.existsByNombreIgnoreCaseAndEliminadoFalse("alfajores"));
        assertFalse(categoriaRepository.existsByNombreIgnoreCase("Chupetines"));

        // Al modificar, no debe chocar consigo misma pero sí con otra categoría distinta
        assertFalse(categoriaRepository.existsByNombreIgnoreCaseAndIdNotAndEliminadoFalse("ALFAJORES", guardada.getId()));
        assertTrue(categoriaRepository.existsByNombreIgnoreCaseAndIdNotAndEliminadoFalse("ALFAJORES", "otro-id-distinto"));
    }
}

