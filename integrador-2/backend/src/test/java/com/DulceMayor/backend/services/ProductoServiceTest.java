package com.DulceMayor.backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.DulceMayor.backend.config.ConflictoException;
import com.DulceMayor.backend.dtos.producto.ProductoCreadoEvent;
import com.DulceMayor.backend.dtos.producto.ProductoMapper;
import com.DulceMayor.backend.dtos.producto.ProductoRequest;
import com.DulceMayor.backend.dtos.producto.ProductoResponse;
import com.DulceMayor.backend.entities.producto.Categoria;
import com.DulceMayor.backend.entities.producto.Producto;
import com.DulceMayor.backend.entities.producto.SubCategoria;
import com.DulceMayor.backend.repositories.producto.ProductoRepository;
import com.DulceMayor.backend.services.producto.ProductoLecturaService;
import com.DulceMayor.backend.services.producto.ProductoService;
import com.DulceMayor.backend.services.producto.SubCategoriaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private SubCategoriaService subCategoriaService;

    @Mock
    private ImagenService imagenService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private ProductoLecturaService productoLecturaService;

    private final ProductoMapper productoMapper = Mappers.getMapper(ProductoMapper.class);

    private ProductoService productoService;

    private SubCategoria subCategoriaMock;

    @BeforeEach
    void setUp() {
        productoService = new ProductoService(
                productoRepository,
                productoMapper,
                subCategoriaService,
                imagenService,
                eventPublisher,
                productoLecturaService
        );

        Categoria cat = Categoria.builder().id("cat-1").nombre("Golosinas").eliminado(false).build();
        subCategoriaMock = SubCategoria.builder().id("sub-1").nombre("Chocolates").categoria(cat).eliminado(false).build();
    }

    @Test
    @DisplayName("crear: éxito emite ProductoCreadoEvent y retorna ProductoResponse")
    void crearProductoExitoso() {
        ProductoRequest req = new ProductoRequest(
                "CHO-01",
                "Chocolate con Maní",
                "Tableta 100g",
                "Caja x12",
                12,
                false,
                null,
                "sub-1",
                500.0,
                100
        );

        when(productoRepository.existsByCodigoIgnoreCaseAndEliminadoFalse("CHO-01")).thenReturn(false);
        when(subCategoriaService.obtenerEntidad("sub-1")).thenReturn(subCategoriaMock);
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> {
            Producto p = invocation.getArgument(0);
            p.setId("prod-1");
            return p;
        });

        ProductoResponse mockEnriquecido = new ProductoResponse(
                "prod-1", "CHO-01", "Chocolate con Maní", "Tableta 100g", "Caja x12", 12, false,
                null, "sub-1", "Chocolates", "cat-1", "Golosinas", 500.0, 100
        );
        when(productoLecturaService.enriquecerResponse(any(Producto.class))).thenReturn(mockEnriquecido);

        ProductoResponse res = productoService.crear(req);

        assertNotNull(res);
        assertEquals("prod-1", res.id());
        assertEquals("CHO-01", res.codigo());

        ArgumentCaptor<ProductoCreadoEvent> eventCaptor = ArgumentCaptor.forClass(ProductoCreadoEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals("prod-1", eventCaptor.getValue().productoId());
        assertEquals(500.0, eventCaptor.getValue().precioInicial());
        assertEquals(100, eventCaptor.getValue().stockInicial());
    }

    @Test
    @DisplayName("crear: código duplicado lanza ConflictoException")
    void crearCodigoDuplicado() {
        ProductoRequest req = new ProductoRequest(
                "CHO-01",
                "Chocolate con Maní",
                null,
                "Caja x12",
                12,
                false,
                null,
                "sub-1",
                null,
                null
        );

        when(productoRepository.existsByCodigoIgnoreCaseAndEliminadoFalse("CHO-01")).thenReturn(true);

        ConflictoException ex = assertThrows(ConflictoException.class, () -> productoService.crear(req));
        assertEquals("DUPLICADO", ex.getCode());
    }
}
