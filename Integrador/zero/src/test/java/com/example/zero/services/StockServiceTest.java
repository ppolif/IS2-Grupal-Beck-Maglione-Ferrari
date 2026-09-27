package com.example.zero.services;

import com.example.zero.entidades.compra.Detalle;
import com.example.zero.entidades.compra.Stock;
import com.example.zero.entidades.producto.Producto;
import com.example.zero.repositories.ProductoRepository;
import com.example.zero.repositories.StockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private StockService stockService;

    @Test
    @DisplayName("calcularStockActual retorna cantidadActual del último registro en Stock si existe")
    void calcularStockActual_conMovimientosPrevios_retornaCantidadActualDelUltimoStock() {
        String prodId = "prod-100";
        Stock ultimoStock = Stock.builder()
                .id("stk-1")
                .cantidadActual(35)
                .eliminado(false)
                .build();

        when(stockRepository.findUltimoStockPorProducto(prodId)).thenReturn(Optional.of(ultimoStock));

        int stock = stockService.calcularStockActual(prodId);

        assertEquals(35, stock);
        verify(stockRepository).findUltimoStockPorProducto(prodId);
        verifyNoInteractions(productoRepository);
    }

    @Test
    @DisplayName("calcularStockActual hace fallback a stock de Producto si no hay registros en Stock")
    void calcularStockActual_sinMovimientosPrevios_retornaStockDeEntidadProducto() {
        String prodId = "prod-200";
        Producto producto = Producto.builder()
                .id(prodId)
                .nombre("Remera Zero")
                .stock(50)
                .eliminado(false)
                .build();

        when(stockRepository.findUltimoStockPorProducto(prodId)).thenReturn(Optional.empty());
        when(productoRepository.findById(prodId)).thenReturn(Optional.of(producto));

        int stock = stockService.calcularStockActual(prodId);

        assertEquals(50, stock);
        verify(stockRepository).findUltimoStockPorProducto(prodId);
        verify(productoRepository).findById(prodId);
    }

    @Test
    @DisplayName("calcularStockActual retorna 0 si el producto no existe o tiene id nulo")
    void calcularStockActual_productoInexistenteOSinStock_retornaCero() {
        assertEquals(0, stockService.calcularStockActual(null));
        assertEquals(0, stockService.calcularStockActual("   "));

        when(stockRepository.findUltimoStockPorProducto("prod-inexistente")).thenReturn(Optional.empty());
        when(productoRepository.findById("prod-inexistente")).thenReturn(Optional.empty());

        assertEquals(0, stockService.calcularStockActual("prod-inexistente"));
    }

    @Test
    @DisplayName("crearStock persiste y retorna la entidad Stock asociada al Detalle")
    void crearStock_conDatosValidos_persisteYRetornaStock() {
        Detalle detalle = Detalle.builder().id("det-1").build();
        Stock stockGuardado = Stock.builder()
                .id("stk-2")
                .detalle(detalle)
                .cantidadActual(18)
                .observacion("Egreso por Venta - Factura N° 1001")
                .eliminado(false)
                .build();

        when(stockRepository.save(any(Stock.class))).thenReturn(stockGuardado);

        Stock resultado = stockService.crearStock(detalle, 18, "Egreso por Venta - Factura N° 1001");

        assertNotNull(resultado);
        assertEquals(18, resultado.getCantidadActual());
        assertEquals("Egreso por Venta - Factura N° 1001", resultado.getObservacion());
        verify(stockRepository).save(any(Stock.class));
    }

    @Test
    @DisplayName("crearStock actualiza el registro existente en lugar de insertar uno nuevo si ya existe para el detalle")
    void crearStock_conStockExistente_actualizaRegistroEnLugarDeDuplicar() {
        Detalle detalle = Detalle.builder().id("det-existente").build();
        Stock stockExistente = Stock.builder()
                .id("stk-existente")
                .detalle(detalle)
                .cantidadActual(10)
                .observacion("Anterior")
                .eliminado(false)
                .build();

        when(stockRepository.findByDetalleId("det-existente")).thenReturn(Optional.of(stockExistente));
        when(stockRepository.save(any(Stock.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Stock resultado = stockService.crearStock(detalle, 25, "Ingreso por Entrega");

        assertNotNull(resultado);
        assertEquals("stk-existente", resultado.getId());
        assertEquals(25, resultado.getCantidadActual());
        assertEquals("Ingreso por Entrega", resultado.getObservacion());
        assertFalse(resultado.isEliminado());
        verify(stockRepository).findByDetalleId("det-existente");
        verify(stockRepository).save(stockExistente);
    }

    @Test
    @DisplayName("crearStock lanza IllegalArgumentException si el balance es negativo")
    void crearStock_conBalanceNegativo_lanzaIllegalArgumentException() {
        Detalle detalle = Detalle.builder().id("det-1").build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                stockService.crearStock(detalle, -5, "Venta"));

        assertEquals("El balance de stock no puede ser negativo", ex.getMessage());
        verifyNoInteractions(stockRepository);
    }

    @Test
    @DisplayName("crearStock lanza IllegalArgumentException si el detalle es nulo")
    void crearStock_conDetalleNulo_lanzaIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                stockService.crearStock(null, 10, "Venta"));

        assertEquals("El detalle de factura no puede ser nulo al registrar stock", ex.getMessage());
        verifyNoInteractions(stockRepository);
    }
}
