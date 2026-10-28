package com.DulceMayor.backend.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.DulceMayor.backend.entities.producto.Categoria;
import com.DulceMayor.backend.entities.producto.SubCategoria;
import java.util.List;

import com.DulceMayor.backend.repositories.producto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SubCategoriaRepositoryTest {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private SubCategoriaRepository subCategoriaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private VigenciaPrecioRepository vigenciaPrecioRepository;

    @Autowired
    private PromocionRepository promocionRepository;

    private Categoria categoria1;
    private Categoria categoria2;

    @BeforeEach
    void setUp() {
        promocionRepository.deleteAll();
        vigenciaPrecioRepository.deleteAll();
        stockRepository.deleteAll();
        productoRepository.deleteAll();
        subCategoriaRepository.deleteAll();
        categoriaRepository.deleteAll();

        categoria1 = categoriaRepository.save(Categoria.builder().nombre("Golosinas").eliminado(false).build());
        categoria2 = categoriaRepository.save(Categoria.builder().nombre("Bebidas").eliminado(false).build());
    }

    @Test
    @DisplayName("existsByNombreIgnoreCaseAndCategoriaIdAndEliminadoFalse: detecta duplicados dentro de la misma categoría sin importar mayúsculas")
    void detectaDuplicadosMismaCategoria() {
        SubCategoria s1 = SubCategoria.builder()
                .nombre("Caramelos")
                .categoria(categoria1)
                .eliminado(false)
                .build();
        subCategoriaRepository.save(s1);

        assertTrue(subCategoriaRepository.existsByNombreIgnoreCaseAndCategoriaIdAndEliminadoFalse("caramelos", categoria1.getId()));
        assertFalse(subCategoriaRepository.existsByNombreIgnoreCaseAndCategoriaIdAndEliminadoFalse("caramelos", categoria2.getId()));
    }

    @Test
    @DisplayName("findAllByCategoriaIdAndEliminadoFalseOrderByNombreAsc: filtra solo activas de la categoría solicitada")
    void filtraPorCategoria() {
        subCategoriaRepository.save(SubCategoria.builder().nombre("Chupetines").categoria(categoria1).eliminado(false).build());
        subCategoriaRepository.save(SubCategoria.builder().nombre("Alfajores").categoria(categoria1).eliminado(false).build());
        subCategoriaRepository.save(SubCategoria.builder().nombre("Gaseosas").categoria(categoria2).eliminado(false).build());

        List<SubCategoria> lista = subCategoriaRepository.findAllByCategoriaIdAndEliminadoFalseOrderByNombreAsc(categoria1.getId());
        assertEquals(2, lista.size());
        assertEquals("Alfajores", lista.get(0).getNombre());
        assertEquals("Chupetines", lista.get(1).getNombre());
    }
}
