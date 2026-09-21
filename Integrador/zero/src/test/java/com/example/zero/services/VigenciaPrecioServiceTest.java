package com.example.zero.services;

import com.example.zero.entidades.producto.Producto;
import com.example.zero.entidades.producto.VigenciaPrecio;
import com.example.zero.repositories.ProductoRepository;
import com.example.zero.repositories.VigenciaPrecioRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VigenciaPrecioServiceTest {

    @Mock
    private VigenciaPrecioRepository vigenciaPrecioRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private VigenciaPrecioService vigenciaPrecioService;

    @Test
    void crearVigenciaPrecio_primerPrecio_guardaVigenciaConFechaHastaNull() {
        // Arrange
        String prodId = "prod-1";
        Producto producto = Producto.builder().id(prodId).nombre("Remera").eliminado(false).build();
        LocalDate hoy = LocalDate.now();

        when(productoRepository.findActive(prodId)).thenReturn(Optional.of(producto));
        when(vigenciaPrecioRepository.findPrecioActualByProductoId(prodId)).thenReturn(Optional.empty());
        when(vigenciaPrecioRepository.save(any(VigenciaPrecio.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        VigenciaPrecio nueva = vigenciaPrecioService.crearVigenciaPrecio(prodId, 15000.0, hoy);

        // Assert
        assertNotNull(nueva);
        assertEquals(15000.0, nueva.getPrecio());
        assertEquals(hoy, nueva.getFechaDesde());
        assertNull(nueva.getFechaHasta());
        assertFalse(nueva.isEliminado());
        assertEquals(producto, nueva.getProducto());
        verify(vigenciaPrecioRepository, times(1)).save(any(VigenciaPrecio.class));
    }

    @Test
    void crearVigenciaPrecio_precioPrevioExistente_cierraVigenciaAnteriorYGuardaNueva() {
        // Arrange
        String prodId = "prod-1";
        Producto producto = Producto.builder().id(prodId).nombre("Remera").eliminado(false).build();
        LocalDate fechaVieja = LocalDate.of(2026, 1, 1);
        LocalDate fechaNueva = LocalDate.of(2026, 3, 1);

        VigenciaPrecio vigenciaPrevia = VigenciaPrecio.builder()
                .id("vig-1")
                .producto(producto)
                .precio(10000.0)
                .fechaDesde(fechaVieja)
                .fechaHasta(null)
                .eliminado(false)
                .build();

        when(productoRepository.findActive(prodId)).thenReturn(Optional.of(producto));
        when(vigenciaPrecioRepository.findPrecioActualByProductoId(prodId)).thenReturn(Optional.of(vigenciaPrevia));
        when(vigenciaPrecioRepository.save(any(VigenciaPrecio.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        VigenciaPrecio nueva = vigenciaPrecioService.crearVigenciaPrecio(prodId, 12500.0, fechaNueva);

        // Assert
        assertEquals(fechaNueva, vigenciaPrevia.getFechaHasta()); // Vigencia anterior cerrada
        assertEquals(12500.0, nueva.getPrecio());
        assertNull(nueva.getFechaHasta()); // Nueva vigencia abierta
        verify(vigenciaPrecioRepository, times(2)).save(any(VigenciaPrecio.class)); // Guarda anterior cerrada + nueva
    }

    @Test
    void crearVigenciaPrecio_precioNegativoOCero_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> vigenciaPrecioService.crearVigenciaPrecio("prod-1", 0.0, LocalDate.now()));
        assertThrows(IllegalArgumentException.class,
                () -> vigenciaPrecioService.crearVigenciaPrecio("prod-1", -10.0, LocalDate.now()));
        verify(vigenciaPrecioRepository, never()).save(any());
    }

    @Test
    void crearVigenciaPrecio_fechaNula_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> vigenciaPrecioService.crearVigenciaPrecio("prod-1", 100.0, null));
    }

    @Test
    void crearVigenciaPrecio_productoInexistente_lanzaIllegalArgumentException() {
        when(productoRepository.findActive("prod-inexistente")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> vigenciaPrecioService.crearVigenciaPrecio("prod-inexistente", 5000.0, LocalDate.now()));
    }

    @Test
    void actualizarPrecio_usaFechaActualYGuardaNuevaVigencia() {
        String prodId = "prod-1";
        Producto producto = Producto.builder().id(prodId).nombre("Pantalón").build();
        when(productoRepository.findActive(prodId)).thenReturn(Optional.of(producto));
        when(vigenciaPrecioRepository.findPrecioActualByProductoId(prodId)).thenReturn(Optional.empty());
        when(vigenciaPrecioRepository.save(any(VigenciaPrecio.class))).thenAnswer(inv -> inv.getArgument(0));

        VigenciaPrecio resultado = vigenciaPrecioService.actualizarPrecio(prodId, 25000.0);

        assertNotNull(resultado);
        assertEquals(25000.0, resultado.getPrecio());
        assertEquals(LocalDate.now(), resultado.getFechaDesde());
        assertNull(resultado.getFechaHasta());
    }

    @Test
    void modificarPrecioVigente_conPrecioValido_actualizaYGuarda() {
        VigenciaPrecio vigencia = VigenciaPrecio.builder().id("vig-1").precio(100.0).eliminado(false).build();
        when(vigenciaPrecioRepository.findActive("vig-1")).thenReturn(Optional.of(vigencia));
        when(vigenciaPrecioRepository.save(any(VigenciaPrecio.class))).thenAnswer(inv -> inv.getArgument(0));

        VigenciaPrecio modificada = vigenciaPrecioService.modificarPrecioVigente("vig-1", 120.0);

        assertEquals(120.0, modificada.getPrecio());
        verify(vigenciaPrecioRepository, times(1)).save(vigencia);
    }

    @Test
    void modificarPrecioVigente_conPrecioInvalido_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> vigenciaPrecioService.modificarPrecioVigente("vig-1", 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> vigenciaPrecioService.modificarPrecioVigente("vig-1", -50.0));
        verify(vigenciaPrecioRepository, never()).save(any());
    }

    @Test
    void eliminarVigencia_existente_marcaEliminado() {
        VigenciaPrecio vigencia = VigenciaPrecio.builder().id("vig-1").eliminado(false).build();
        when(vigenciaPrecioRepository.findActive("vig-1")).thenReturn(Optional.of(vigencia));

        vigenciaPrecioService.eliminarVigencia("vig-1");

        assertTrue(vigencia.isEliminado());
        verify(vigenciaPrecioRepository, times(1)).save(vigencia);
    }

    @Test
    void buscarVigenciaActual_existente_retornaVigencia() {
        VigenciaPrecio vigencia = VigenciaPrecio.builder().id("vig-1").precio(5000.0).fechaHasta(null).build();
        when(vigenciaPrecioRepository.findPrecioActualByProductoId("prod-1")).thenReturn(Optional.of(vigencia));

        VigenciaPrecio actual = vigenciaPrecioService.buscarVigenciaActual("prod-1");

        assertNotNull(actual);
        assertEquals(5000.0, actual.getPrecio());
    }

    @Test
    void buscarVigenciaActual_inexistente_lanzaIllegalArgumentException() {
        when(vigenciaPrecioRepository.findPrecioActualByProductoId("prod-1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> vigenciaPrecioService.buscarVigenciaActual("prod-1"));
    }

    @Test
    void obtenerPrecioActual_retornaMonto() {
        VigenciaPrecio vigencia = VigenciaPrecio.builder().id("vig-1").precio(9999.0).fechaHasta(null).build();
        when(vigenciaPrecioRepository.findPrecioActualByProductoId("prod-1")).thenReturn(Optional.of(vigencia));

        double precio = vigenciaPrecioService.obtenerPrecioActual("prod-1");

        assertEquals(9999.0, precio);
    }

    @Test
    void listarHistorialPrecios_retornaListaOrdenada() {
        List<VigenciaPrecio> historial = List.of(
                VigenciaPrecio.builder().id("v2").precio(200.0).fechaDesde(LocalDate.of(2026, 3, 1)).build(),
                VigenciaPrecio.builder().id("v1").precio(150.0).fechaDesde(LocalDate.of(2026, 1, 1)).build()
        );
        when(vigenciaPrecioRepository.findByProductoIdAndEliminadoFalseOrderByFechaDesdeDesc("prod-1"))
                .thenReturn(historial);

        List<VigenciaPrecio> resultado = vigenciaPrecioService.listarHistorialPrecios("prod-1");

        assertEquals(2, resultado.size());
        assertEquals(200.0, resultado.get(0).getPrecio());
    }
}

