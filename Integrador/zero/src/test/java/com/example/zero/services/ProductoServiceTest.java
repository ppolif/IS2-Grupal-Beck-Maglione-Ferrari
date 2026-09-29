package com.example.zero.services;

import com.example.zero.entidades.Imagen;
import com.example.zero.entidades.producto.Producto;
import com.example.zero.entidades.producto.SubCategoria;
import com.example.zero.enums.TipoImagen;
import com.example.zero.repositories.ProductoRepository;
import com.example.zero.services.producto.ProductoService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private SubCategoriaService subCategoriaService;

    @Mock
    private VigenciaPrecioService vigenciaPrecioService;

    @Mock
    private ImagenService imagenService;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private ProductoService productoService;

    @Test
    void crearProducto_conDatosValidos_creaProductoYVigenciaInicial() {
        // Arrange
        String subId = "sub-1";
        SubCategoria subCategoria = SubCategoria.builder().id(subId).nombre("Remeras").build();
        when(productoRepository.findByCodigoAndEliminadoFalse("REM-001")).thenReturn(Optional.empty());
        when(subCategoriaService.buscarPorId(subId)).thenReturn(subCategoria);
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> {
            Producto p = inv.getArgument(0);
            p.setId("prod-1");
            return p;
        });

        // Act
        Producto resultado = productoService.crearProducto(
                "REM-001", "Remera Básica", "Remera de algodón", "M", subId, 15000.0, false
        );

        // Assert
        assertNotNull(resultado);
        assertEquals("prod-1", resultado.getId());
        assertEquals("REM-001", resultado.getCodigo());
        assertEquals("Remera Básica", resultado.getNombre());
        assertEquals(subCategoria, resultado.getSubCategoria());
        assertFalse(resultado.isEliminado());

        verify(productoRepository, times(1)).save(any(Producto.class));
        verify(vigenciaPrecioService, times(1)).crearVigenciaPrecio(eq("prod-1"), eq(15000.0), any(LocalDate.class));
    }

    @Test
    void crearProducto_conImagen_asociaImagenYTipoProducto() {
        String subId = "sub-1";
        SubCategoria subCategoria = SubCategoria.builder().id(subId).nombre("Remeras").build();
        MockMultipartFile file = new MockMultipartFile("imagen", "remera.jpg", "image/jpeg", new byte[]{1, 2});
        Imagen imagenMock = Imagen.builder().id("img-prod-1").tipoImagen(TipoImagen.PRODUCTO).build();

        when(productoRepository.findByCodigoAndEliminadoFalse("REM-002")).thenReturn(Optional.empty());
        when(subCategoriaService.buscarPorId(subId)).thenReturn(subCategoria);
        when(imagenService.guardarImagen(file, TipoImagen.PRODUCTO)).thenReturn(imagenMock);
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> {
            Producto p = inv.getArgument(0);
            p.setId("prod-2");
            return p;
        });

        Producto resultado = productoService.crearProducto(
                "REM-002", "Remera Estampada", "Desc", "L", subId, 18000.0, false, file
        );

        assertNotNull(resultado);
        assertEquals(1, resultado.getImagenes().size());
        assertEquals("img-prod-1", resultado.getImagenes().get(0).getId());
        verify(imagenService).guardarImagen(file, TipoImagen.PRODUCTO);
    }

    @Test
    void crearProducto_conImagenNulaOVacia_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () ->
                productoService.crearProducto("REM-003", "Nom", "Desc", "S", "sub-1", 100.0, false, null));

        MockMultipartFile emptyFile = new MockMultipartFile("imagen", "", "image/jpeg", new byte[0]);
        assertThrows(IllegalArgumentException.class, () ->
                productoService.crearProducto("REM-003", "Nom", "Desc", "S", "sub-1", 100.0, false, emptyFile));
    }

    @Test
    void crearProducto_codigoDuplicado_lanzaIllegalArgumentException() {
        when(productoRepository.findByCodigoAndEliminadoFalse("REM-001"))
                .thenReturn(Optional.of(Producto.builder().id("p-existente").codigo("REM-001").build()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                productoService.crearProducto("REM-001", "Remera", "desc", "M", "sub-1", 1000.0, false));

        assertTrue(ex.getMessage().contains("Ya existe un producto activo"));
        verify(productoRepository, never()).save(any());
        verify(vigenciaPrecioService, never()).crearVigenciaPrecio(any(), anyDouble(), any());
    }

    @Test
    void crearProducto_precioInicialInvalido_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                productoService.crearProducto("REM-001", "Remera", "desc", "M", "sub-1", 0.0, false));
        assertThrows(IllegalArgumentException.class, () ->
                productoService.crearProducto("REM-001", "Remera", "desc", "M", "sub-1", -50.0, false));
    }

    @Test
    void crearProducto_camposObligatoriosVacios_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                productoService.crearProducto("", "Remera", "desc", "M", "sub-1", 1000.0, false));
        assertThrows(IllegalArgumentException.class, () ->
                productoService.crearProducto("REM-001", "", "desc", "M", "sub-1", 1000.0, false));
        assertThrows(IllegalArgumentException.class, () ->
                productoService.crearProducto("REM-001", "Remera", "desc", "M", "", 1000.0, false));
    }

    @Test
    void modificarProducto_conDatosValidos_actualizaCampos() {
        // Arrange
        SubCategoria sub2 = SubCategoria.builder().id("sub-2").nombre("Buzos").build();
        Producto producto = Producto.builder()
                .id("prod-1")
                .nombre("Remera Vieja")
                .descripcion("Vieja")
                .talle("S")
                .enOferta(false)
                .eliminado(false)
                .build();

        when(productoRepository.findActive("prod-1")).thenReturn(Optional.of(producto));
        when(subCategoriaService.buscarPorId("sub-2")).thenReturn(sub2);
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Producto modificado = productoService.modificarProducto(
                "prod-1", "Remera Nueva", "Nueva desc", "L", "sub-2", true
        );

        // Assert
        assertEquals("Remera Nueva", modificado.getNombre());
        assertEquals("Nueva desc", modificado.getDescripcion());
        assertEquals("L", modificado.getTalle());
        assertEquals(sub2, modificado.getSubCategoria());
        assertTrue(modificado.isEnOferta());
        verify(productoRepository, times(1)).save(producto);
    }

    @Test
    void modificarProducto_inexistente_lanzaIllegalArgumentException() {
        when(productoRepository.findActive("prod-inexistente")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                productoService.modificarProducto("prod-inexistente", "Nombre", null, null, null, null));
    }

    @Test
    void eliminarProducto_existente_marcaEliminado() {
        Producto producto = Producto.builder().id("prod-1").eliminado(false).build();
        when(productoRepository.findActive("prod-1")).thenReturn(Optional.of(producto));

        productoService.eliminarProducto("prod-1");

        assertTrue(producto.isEliminado());
        verify(productoRepository, times(1)).save(producto);
    }

    @Test
    void buscarPorId_existente_retornaProducto() {
        Producto producto = Producto.builder().id("prod-1").nombre("Camisa").build();
        when(productoRepository.findActive("prod-1")).thenReturn(Optional.of(producto));

        Producto encontrado = productoService.buscarPorId("prod-1");

        assertEquals("Camisa", encontrado.getNombre());
    }

    @Test
    void buscarPorId_inexistente_lanzaIllegalArgumentException() {
        when(productoRepository.findActive("prod-inexistente")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> productoService.buscarPorId("prod-inexistente"));
    }

    @Test
    void buscarPorCodigo_existente_retornaProducto() {
        Producto producto = Producto.builder().id("prod-1").codigo("COD-99").build();
        when(productoRepository.findByCodigoAndEliminadoFalse("COD-99")).thenReturn(Optional.of(producto));

        Producto encontrado = productoService.buscarPorCodigo("COD-99");

        assertEquals("COD-99", encontrado.getCodigo());
    }

    @Test
    void listarActivos_retornaListaDeProductos() {
        when(productoRepository.findByEliminadoFalse()).thenReturn(List.of(
                Producto.builder().id("p1").build(),
                Producto.builder().id("p2").build()
        ));

        List<Producto> activos = productoService.listarActivos();

        assertEquals(2, activos.size());
    }

    @Test
    void listarEnOferta_retornaProductosEnOferta() {
        when(productoRepository.findByEnOfertaTrueAndEliminadoFalse()).thenReturn(List.of(
                Producto.builder().id("p1").enOferta(true).build()
        ));

        List<Producto> ofertas = productoService.listarEnOferta();

        assertEquals(1, ofertas.size());
        assertTrue(ofertas.get(0).isEnOferta());
    }

    @Test
    void marcarEnOferta_actualizaEstadoOferta() {
        Producto producto = Producto.builder().id("prod-1").enOferta(false).build();
        when(productoRepository.findActive("prod-1")).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        Producto actualizado = productoService.marcarEnOferta("prod-1", true);

        assertTrue(actualizado.isEnOferta());
        verify(productoRepository, times(1)).save(producto);
    }

    // ==================== TESTS DE PRECIOS E INFLACIÓN ====================

    @Test
    void actualizarPrecio_delegaAVigenciaPrecioService() {
        Producto producto = Producto.builder().id("prod-1").build();
        when(productoRepository.findActive("prod-1")).thenReturn(Optional.of(producto));

        productoService.actualizarPrecio("prod-1", 18000.0);

        verify(vigenciaPrecioService, times(1)).actualizarPrecio("prod-1", 18000.0);
    }

    @Test
    void aplicarAumentoPorInflacion_calculaNuevoPrecioYActualiza() {
        // Arrange (Aumento bimestral por inflación del 10% sobre $10.000 -> $11.000)
        String prodId = "prod-1";
        Producto producto = Producto.builder().id(prodId).build();
        when(productoRepository.findActive(prodId)).thenReturn(Optional.of(producto));
        when(vigenciaPrecioService.obtenerPrecioActual(prodId)).thenReturn(10000.0);

        // Act
        double nuevoPrecio = productoService.aplicarAumentoPorInflacion(prodId, 10.0);

        // Assert
        assertEquals(11000.0, nuevoPrecio, 0.001);
        verify(vigenciaPrecioService, times(1)).actualizarPrecio(prodId, 11000.0);
    }

    @Test
    void aplicarAumentoPorInflacion_porcentajeInvalido_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> productoService.aplicarAumentoPorInflacion("prod-1", 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> productoService.aplicarAumentoPorInflacion("prod-1", -5.0));
    }

    @Test
    void aplicarAumentoGeneralPorInflacion_aplicaATodosLosActivosPaginados() {
        Producto p1 = Producto.builder().id("p1").build();
        Producto p2 = Producto.builder().id("p2").build();
        Page<Producto> pagina = new PageImpl<>(List.of(p1, p2), PageRequest.of(0, 100, org.springframework.data.domain.Sort.by("id").ascending()), 2);

        when(productoRepository.findByEliminadoFalse(any(Pageable.class))).thenReturn(pagina);
        when(productoRepository.findActive("p1")).thenReturn(Optional.of(p1));
        when(productoRepository.findActive("p2")).thenReturn(Optional.of(p2));
        when(vigenciaPrecioService.obtenerPrecioActual("p1")).thenReturn(100.0);
        when(vigenciaPrecioService.obtenerPrecioActual("p2")).thenReturn(200.0);

        int actualizados = productoService.aplicarAumentoGeneralPorInflacion(15.0);

        assertEquals(2, actualizados);
        verify(vigenciaPrecioService, times(1)).actualizarPrecio("p1", 115.0);
        verify(vigenciaPrecioService, times(1)).actualizarPrecio("p2", 230.0);
        verify(entityManager, times(1)).flush();
        verify(entityManager, times(1)).clear();
    }

    @Test
    void aplicarAumentoGeneralPorInflacion_conMultiplesPaginas_iteraYActualizaTodos() {
        Producto p1 = Producto.builder().id("p1").build();
        Producto p2 = Producto.builder().id("p2").build();
        Producto p3 = Producto.builder().id("p3").build();

        Pageable page0 = PageRequest.of(0, 2, org.springframework.data.domain.Sort.by("id").ascending());
        Pageable page1 = PageRequest.of(1, 2, org.springframework.data.domain.Sort.by("id").ascending());

        Page<Producto> pagina0 = new PageImpl<>(List.of(p1, p2), page0, 3);
        Page<Producto> pagina1 = new PageImpl<>(List.of(p3), page1, 3);

        when(productoRepository.findByEliminadoFalse(page0)).thenReturn(pagina0);
        when(productoRepository.findByEliminadoFalse(page1)).thenReturn(pagina1);

        when(productoRepository.findActive("p1")).thenReturn(Optional.of(p1));
        when(productoRepository.findActive("p2")).thenReturn(Optional.of(p2));
        when(productoRepository.findActive("p3")).thenReturn(Optional.of(p3));

        when(vigenciaPrecioService.obtenerPrecioActual("p1")).thenReturn(1000.0);
        when(vigenciaPrecioService.obtenerPrecioActual("p2")).thenReturn(2000.0);
        when(vigenciaPrecioService.obtenerPrecioActual("p3")).thenReturn(3000.0);

        int total = productoService.aplicarAumentoGeneralPorInflacion(10.0, 2);

        assertEquals(3, total);
        verify(vigenciaPrecioService, times(1)).actualizarPrecio("p1", 1100.0);
        verify(vigenciaPrecioService, times(1)).actualizarPrecio("p2", 2200.0);
        verify(vigenciaPrecioService, times(1)).actualizarPrecio("p3", 3300.0);
        verify(entityManager, times(2)).flush();
        verify(entityManager, times(2)).clear();
    }

    @Test
    void aplicarAumentoGeneralPorInflacion_catalogoVacio_retornaCero() {
        Page<Producto> paginaVacia = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 100), 0);
        when(productoRepository.findByEliminadoFalse(any(Pageable.class))).thenReturn(paginaVacia);

        int total = productoService.aplicarAumentoGeneralPorInflacion(10.0);

        assertEquals(0, total);
        verify(vigenciaPrecioService, never()).actualizarPrecio(anyString(), anyDouble());
    }

    @Test
    void aplicarAumentoGeneralPorInflacion_porcentajeInvalido_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                productoService.aplicarAumentoGeneralPorInflacion(0.0));
        assertThrows(IllegalArgumentException.class, () ->
                productoService.aplicarAumentoGeneralPorInflacion(-10.0));
    }

    @Test
    void actualizarPreciosPorInflacionProgramado_ejecutaAumentoBimestral() {
        Producto p1 = Producto.builder().id("p1").build();
        Page<Producto> pagina = new PageImpl<>(List.of(p1), PageRequest.of(0, 100, org.springframework.data.domain.Sort.by("id").ascending()), 1);

        when(productoRepository.findByEliminadoFalse(any(Pageable.class))).thenReturn(pagina);
        when(productoRepository.findActive("p1")).thenReturn(Optional.of(p1));
        when(vigenciaPrecioService.obtenerPrecioActual("p1")).thenReturn(100.0);

        productoService.setPorcentajeBimestral(8.0);
        productoService.setDefaultBatchSize(100);

        productoService.actualizarPreciosPorInflacionProgramado();

        verify(vigenciaPrecioService, times(1)).actualizarPrecio("p1", 108.0);
    }

    @Test
    void helperMethods_obtenerDatosParaVista() {
        SubCategoria sub = SubCategoria.builder()
                .nombre("Running")
                .categoria(com.example.zero.entidades.producto.Categoria.builder().nombre("Calzado").build())
                .build();
        Imagen img = Imagen.builder().id("img-prod-99").eliminado(false).build();
        Producto producto = Producto.builder()
                .id("prod-99")
                .nombre("Zapatilla")
                .subCategoria(sub)
                .imagenes(List.of(img))
                .build();

        when(vigenciaPrecioService.obtenerPrecioActual("prod-99")).thenReturn(25000.0);

        double precio = productoService.obtenerPrecioActual(producto);
        String categoria = productoService.obtenerNombreCategoria(producto);
        String imgUrl = productoService.obtenerImagenUrl(producto);

        assertEquals(25000.0, precio);
        assertEquals("Calzado", categoria);
        assertEquals("/imagen/img-prod-99", imgUrl);
    }
}
