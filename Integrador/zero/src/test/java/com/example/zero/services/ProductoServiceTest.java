package com.example.zero.services;

import com.example.zero.entidades.producto.Producto;
import com.example.zero.entidades.producto.SubCategoria;
import com.example.zero.repositories.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
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
    void aplicarAumentoGeneralPorInflacion_aplicaATodosLosActivos() {
        Producto p1 = Producto.builder().id("p1").build();
        Producto p2 = Producto.builder().id("p2").build();
        when(productoRepository.findByEliminadoFalse()).thenReturn(List.of(p1, p2));
        when(productoRepository.findActive("p1")).thenReturn(Optional.of(p1));
        when(productoRepository.findActive("p2")).thenReturn(Optional.of(p2));
        when(vigenciaPrecioService.obtenerPrecioActual("p1")).thenReturn(100.0);
        when(vigenciaPrecioService.obtenerPrecioActual("p2")).thenReturn(200.0);

        productoService.aplicarAumentoGeneralPorInflacion(15.0);

        verify(vigenciaPrecioService, times(1)).actualizarPrecio("p1", 115.0);
        verify(vigenciaPrecioService, times(1)).actualizarPrecio("p2", 230.0);
    }

    // ==================== TESTS DE STOCK E INVENTARIO ====================

    @Test
    void esStockCritico_menorAl20PorCiento_retornaTrue() {
        // 19 de 100 es 19% (< 20%) -> crítico
        assertTrue(productoService.esStockCritico(19, 100));
        // 0 de 100 -> crítico
        assertTrue(productoService.esStockCritico(0, 100));
        // 1 de 10 es 10% -> crítico
        assertTrue(productoService.esStockCritico(1, 10));
    }

    @Test
    void esStockCritico_mayorOIgualAl20PorCiento_retornaFalse() {
        // 20 de 100 es 20% -> no crítico
        assertFalse(productoService.esStockCritico(20, 100));
        // 50 de 100 es 50% -> no crítico
        assertFalse(productoService.esStockCritico(50, 100));
    }

    @Test
    void esStockCritico_stockTotalInvalido_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> productoService.esStockCritico(5, 0));
        assertThrows(IllegalArgumentException.class, () -> productoService.esStockCritico(5, -10));
        assertThrows(IllegalArgumentException.class, () -> productoService.esStockCritico(-1, 100));
    }

    @Test
    void disminuirStock_conStockSuficiente_restaCantidad() {
        // Al confirmarse un pago de cliente
        int stockActual = 50;
        int cantidadComprada = 5;

        int nuevoStock = productoService.disminuirStock(stockActual, cantidadComprada);

        assertEquals(45, nuevoStock);
    }

    @Test
    void disminuirStock_conStockInsuficiente_lanzaIllegalArgumentException() {
        int stockActual = 3;
        int cantidadComprada = 5;

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> productoService.disminuirStock(stockActual, cantidadComprada));
        assertTrue(ex.getMessage().contains("Stock insuficiente"));
    }

    @Test
    void disminuirStock_cantidadInvalida_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> productoService.disminuirStock(10, 0));
        assertThrows(IllegalArgumentException.class, () -> productoService.disminuirStock(10, -3));
    }

    @Test
    void aumentarStock_conCantidadValida_sumaCantidad() {
        // Al recibir una orden de compra de proveedor
        int stockActual = 10;
        int cantidadRecibida = 40;

        int nuevoStock = productoService.aumentarStock(stockActual, cantidadRecibida);

        assertEquals(50, nuevoStock);
    }

    @Test
    void aumentarStock_cantidadInvalida_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> productoService.aumentarStock(10, 0));
        assertThrows(IllegalArgumentException.class, () -> productoService.aumentarStock(10, -5));
        assertThrows(IllegalArgumentException.class, () -> productoService.aumentarStock(-1, 10));
    }
}

