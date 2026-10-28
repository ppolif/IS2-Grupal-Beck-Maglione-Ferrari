package com.DulceMayor.backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.DulceMayor.backend.config.ConflictoException;
import com.DulceMayor.backend.dtos.producto.ProductoCreadoEvent;
import com.DulceMayor.backend.dtos.producto.StockAjusteRequest;
import com.DulceMayor.backend.dtos.producto.StockMapper;
import com.DulceMayor.backend.dtos.producto.StockReposicionRequest;
import com.DulceMayor.backend.dtos.producto.StockResponse;
import com.DulceMayor.backend.entities.producto.Producto;
import com.DulceMayor.backend.entities.producto.Stock;
import com.DulceMayor.backend.repositories.producto.StockRepository;
import java.time.LocalDateTime;
import java.util.Optional;

import com.DulceMayor.backend.services.producto.ProductoService;
import com.DulceMayor.backend.services.producto.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private ProductoService productoService;

    private final StockMapper stockMapper = Mappers.getMapper(StockMapper.class);

    private StockService stockService;

    private Producto productoMock;
    private Stock stockMock;

    @BeforeEach
    void setUp() {
        stockService = new StockService(stockRepository, stockMapper, productoService);
        productoMock = Producto.builder()
                .id("prod-1")
                .nombre("Alfajor Triple")
                .codigo("ALF-01")
                .eliminado(false)
                .build();
        stockMock = Stock.builder()
                .id("stock-1")
                .cantidadActual(50)
                .fechaHora(LocalDateTime.now())
                .observacion("Ingreso - Stock inicial")
                .eliminado(false)
                .build();
    }

    @Test
    @DisplayName("Observer: alCrearProducto genera nuevo registro inmutable con stock inicial")
    void alCrearProductoRegistraMovimientoInicial() {
        when(productoService.obtenerEntidad("prod-1")).thenReturn(productoMock);

        stockService.alCrearProducto(new ProductoCreadoEvent("prod-1", 1500.0, 100));

        ArgumentCaptor<Stock> captor = ArgumentCaptor.forClass(Stock.class);
        verify(stockRepository).save(captor.capture());
        Stock guardado = captor.getValue();
        assertEquals(100, guardado.getCantidadActual());
        assertNotNull(guardado.getFechaHora());
        assertTrue(guardado.getObservacion().contains("Ingreso - Stock inicial"));
    }

    @Test
    @DisplayName("modificarStock: ajuste físico genera un nuevo registro en el historial")
    void modificarStockAjuste() {
        StockAjusteRequest req = new StockAjusteRequest(30, "Conteo físico mensual");
        when(productoService.obtenerEntidad("prod-1")).thenReturn(productoMock);
        when(stockRepository.findFirstByEliminadoFalseOrderByFechaHoraDescIdDesc())
                .thenReturn(Optional.of(stockMock));
        when(stockRepository.save(any(Stock.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StockResponse res = stockService.modificarStock("prod-1", req);

        assertNotNull(res);
        assertEquals(30, res.cantidadActual());
        assertEquals("Conteo físico mensual", res.observacion());
        assertNotNull(res.fechaHora());
    }

    @Test
    @DisplayName("reponerStock: genera nuevo registro sumando la cantidad al stock previo")
    void reponerStockSumaCantidad() {
        StockReposicionRequest req = new StockReposicionRequest(25, "Llegada de proveedor");
        when(productoService.obtenerEntidad("prod-1")).thenReturn(productoMock);
        when(stockRepository.findFirstByEliminadoFalseOrderByFechaHoraDescIdDesc())
                .thenReturn(Optional.of(stockMock));
        when(stockRepository.save(any(Stock.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StockResponse res = stockService.reponerStock("prod-1", req);

        assertNotNull(res);
        assertEquals(75, res.cantidadActual()); // 50 + 25
        assertEquals("Llegada de proveedor", res.observacion());
    }

    @Test
    @DisplayName("descontar: genera un nuevo registro de egreso con la cantidad descontada")
    void descontarExitoso() {
        when(stockRepository.findFirstByEliminadoFalseOrderByFechaHoraDescIdDesc())
                .thenReturn(Optional.of(stockMock));

        stockService.descontar("prod-1", 20);

        ArgumentCaptor<Stock> captor = ArgumentCaptor.forClass(Stock.class);
        verify(stockRepository).save(captor.capture());
        Stock nuevoMovimiento = captor.getValue();
        assertEquals(30, nuevoMovimiento.getCantidadActual()); // 50 - 20
        assertTrue(nuevoMovimiento.getObservacion().startsWith("Egreso"));
    }

    @Test
    @DisplayName("descontar: stock insuficiente lanza ConflictoException")
    void descontarInsuficienteLanzaConflicto() {
        when(stockRepository.findFirstByEliminadoFalseOrderByFechaHoraDescIdDesc())
                .thenReturn(Optional.of(stockMock));

        ConflictoException ex = assertThrows(ConflictoException.class, () -> stockService.descontar("prod-1", 100));
        assertEquals("STOCK_INSUFICIENTE", ex.getCode());
    }

    @Test
    @DisplayName("hayStock: verifica disponibilidad sobre el último movimiento")
    void hayStockVerificacion() {
        when(stockRepository.findFirstByEliminadoFalseOrderByFechaHoraDescIdDesc())
                .thenReturn(Optional.of(stockMock));

        assertTrue(stockService.hayStock("prod-1", 50));
        assertFalse(stockService.hayStock("prod-1", 51));
    }
}
