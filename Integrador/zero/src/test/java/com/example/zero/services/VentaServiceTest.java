package com.example.zero.services;

import com.example.zero.entidades.compra.Detalle;
import com.example.zero.entidades.compra.Factura;
import com.example.zero.entidades.compra.FormaDePago;
import com.example.zero.entidades.persona.Cliente;
import com.example.zero.entidades.persona.Nacionalidad;
import com.example.zero.entidades.producto.Producto;
import com.example.zero.enums.EstadoFactura;
import com.example.zero.enums.TipoDePago;
import com.example.zero.enums.TipoDocumento;
import com.example.zero.repositories.ClienteRepository;
import com.example.zero.repositories.DetalleRepository;
import com.example.zero.repositories.FacturaRepository;
import com.example.zero.repositories.FormaDePagoRepository;
import com.example.zero.repositories.NacionalidadRepository;
import com.example.zero.services.persona.ClienteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock
    private FacturaRepository facturaRepository;

    @Mock
    private DetalleRepository detalleRepository;

    @Mock
    private FormaDePagoRepository formaDePagoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ClienteService clienteService;

    @Mock
    private NacionalidadRepository nacionalidadRepository;

    @Mock
    private ProductoService productoService;

    @InjectMocks
    private VentaService ventaService;

    @Test
    void validarVenta_conDatosValidos_noLanzaExcepcion() {
        assertDoesNotThrow(() -> ventaService.validarVenta(
                "12345678", "Juan", "Perez",
                List.of("prod-1"), List.of(2)
        ));
    }

    @Test
    void validarVenta_conDniVacio_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> ventaService.validarVenta(
                "", "Juan", "Perez",
                List.of("prod-1"), List.of(1)
        ));
        assertThrows(IllegalArgumentException.class, () -> ventaService.validarVenta(
                null, "Juan", "Perez",
                List.of("prod-1"), List.of(1)
        ));
    }

    @Test
    void validarVenta_conNombreOApellidoVacio_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> ventaService.validarVenta(
                "12345678", "  ", "Perez",
                List.of("prod-1"), List.of(1)
        ));
        assertThrows(IllegalArgumentException.class, () -> ventaService.validarVenta(
                "12345678", "Juan", "",
                List.of("prod-1"), List.of(1)
        ));
    }

    @Test
    void validarVenta_conProductosVacios_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> ventaService.validarVenta(
                "12345678", "Juan", "Perez",
                Collections.emptyList(), Collections.emptyList()
        ));
        assertThrows(IllegalArgumentException.class, () -> ventaService.validarVenta(
                "12345678", "Juan", "Perez",
                null, List.of(1)
        ));
    }

    @Test
    void validarVenta_conCantidadesDesiguales_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> ventaService.validarVenta(
                "12345678", "Juan", "Perez",
                List.of("prod-1"), List.of(1, 2)
        ));
    }

    @Test
    void validarVenta_conCantidadInvalida_lanzaIllegalArgumentException() {
        List<String> prods = List.of("prod-1");
        List<Integer> cantCero = List.of(0);
        assertThrows(IllegalArgumentException.class, () -> ventaService.validarVenta(
                "12345678", "Juan", "Perez", prods, cantCero
        ));
    }

    @Test
    void registrarVenta_conClienteExistente_reutilizaClienteYPersisteFactura() {
        Cliente clienteExistente = Cliente.builder()
                .numeroDocumento("12345678")
                .nombre("Juan")
                .apellido("Perez")
                .eliminado(false)
                .build();
        when(clienteRepository.findByNumeroDocumentoAndEliminadoFalse("12345678"))
                .thenReturn(Optional.of(clienteExistente));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(i -> i.getArgument(0));

        FormaDePago formaExistente = FormaDePago.builder()
                .id("fdp-1")
                .tipoPago(TipoDePago.EFECTIVO)
                .eliminado(false)
                .build();
        when(formaDePagoRepository.findByTipoPagoAndEliminadoFalse(TipoDePago.EFECTIVO))
                .thenReturn(Optional.of(formaExistente));

        Factura ultimaFactura = Factura.builder().numeroFactura(1050L).build();
        when(facturaRepository.findTopByOrderByNumeroFacturaDesc()).thenReturn(Optional.of(ultimaFactura));

        Producto prod = Producto.builder().id("p1").nombre("Zapatillas").build();
        when(productoService.buscarPorId("p1")).thenReturn(prod);
        when(productoService.obtenerPrecioActual("p1")).thenReturn(2500.0);

        when(facturaRepository.save(any(Factura.class))).thenAnswer(i -> i.getArgument(0));

        Factura resultado = ventaService.registrarVenta(
                "12345678", "Juan", "Perez", "juan@test.com", "EFECTIVO",
                List.of("p1"), List.of(2)
        );

        assertNotNull(resultado);
        assertEquals(1051L, resultado.getNumeroFactura());
        assertEquals(5000.0, resultado.getTotalPagado());
        assertEquals(EstadoFactura.PAGADA, resultado.getEstado());
        assertEquals(clienteExistente, resultado.getCliente());
        assertEquals(formaExistente, resultado.getFormaDePago());
        assertEquals(1, resultado.getDetalles().size());

        verify(clienteService, never()).crearCliente(any(), any(), any(), any(), any(), any());
        verify(facturaRepository).save(any(Factura.class));
    }

    @Test
    void registrarVenta_conClienteNuevo_creaClienteYPersisteFactura() {
        when(clienteRepository.findByNumeroDocumentoAndEliminadoFalse("87654321"))
                .thenReturn(Optional.empty());

        Nacionalidad nac = Nacionalidad.builder().id("nac-1").nombre("Argentina").build();
        when(nacionalidadRepository.findByNombreAndEliminadoFalse("Argentina")).thenReturn(Optional.of(nac));

        Cliente clienteCreado = Cliente.builder()
                .numeroDocumento("87654321")
                .nombre("Maria")
                .apellido("Gomez")
                .nacionalidad(nac)
                .eliminado(false)
                .build();
        when(clienteService.crearCliente(eq("87654321"), eq("Maria"), eq("Gomez"), any(), eq(TipoDocumento.DNI), eq(nac)))
                .thenReturn(clienteCreado);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(i -> i.getArgument(0));

        when(formaDePagoRepository.findByTipoPagoAndEliminadoFalse(TipoDePago.TARJETA_DEBITO))
                .thenReturn(Optional.empty());
        when(formaDePagoRepository.save(any(FormaDePago.class))).thenAnswer(i -> {
            FormaDePago fdp = i.getArgument(0);
            fdp.setId("fdp-new");
            return fdp;
        });

        when(facturaRepository.findTopByOrderByNumeroFacturaDesc()).thenReturn(Optional.empty());

        Producto prod = Producto.builder().id("p2").nombre("Remera").build();
        when(productoService.buscarPorId("p2")).thenReturn(prod);
        when(productoService.obtenerPrecioActual("p2")).thenReturn(1500.0);

        when(facturaRepository.save(any(Factura.class))).thenAnswer(i -> i.getArgument(0));

        Factura resultado = ventaService.registrarVenta(
                "87654321", "Maria", "Gomez", "maria@test.com", "TARJETA_DEBITO",
                List.of("p2"), List.of(3)
        );

        assertNotNull(resultado);
        assertEquals(1001L, resultado.getNumeroFactura());
        assertEquals(4500.0, resultado.getTotalPagado());
        verify(clienteService, times(1)).crearCliente(any(), any(), any(), any(), any(), any());
    }

    @Test
    void listarVentas_retornaListaFacturasOrdenadas() {
        Factura f1 = Factura.builder().id("fac-1").numeroFactura(1001L).build();
        Factura f2 = Factura.builder().id("fac-2").numeroFactura(1002L).build();
        when(facturaRepository.findByEliminadoFalseOrderByFechaFacturaDesc()).thenReturn(List.of(f2, f1));

        List<Factura> resultado = ventaService.listarVentas();

        assertEquals(2, resultado.size());
        assertEquals("fac-2", resultado.get(0).getId());
        verify(facturaRepository).findByEliminadoFalseOrderByFechaFacturaDesc();
    }

    @Test
    void buscarPorId_existente_retornaFactura() {
        Factura f = Factura.builder().id("fac-1").numeroFactura(1001L).build();
        when(facturaRepository.findActive("fac-1")).thenReturn(Optional.of(f));

        Factura res = ventaService.buscarPorId("fac-1");
        assertNotNull(res);
        assertEquals(1001L, res.getNumeroFactura());
    }

    @Test
    void buscarPorId_inexistente_lanzaExcepcion() {
        when(facturaRepository.findActive("fac-99")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> ventaService.buscarPorId("fac-99"));
    }

    @Test
    void buscarPorId_nuloOVacio_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> ventaService.buscarPorId(null));
        assertThrows(IllegalArgumentException.class, () -> ventaService.buscarPorId("  "));
    }

    @Test
    void buscarPorNumeroFactura_existente_retornaFactura() {
        Factura f = Factura.builder().id("fac-1").numeroFactura(1005L).build();
        when(facturaRepository.findByNumeroFacturaAndEliminadoFalse(1005L)).thenReturn(Optional.of(f));

        Factura res = ventaService.buscarPorNumeroFactura(1005L);
        assertNotNull(res);
        assertEquals("fac-1", res.getId());
    }

    @Test
    void buscarPorNumeroFactura_inexistente_lanzaExcepcion() {
        when(facturaRepository.findByNumeroFacturaAndEliminadoFalse(9999L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> ventaService.buscarPorNumeroFactura(9999L));
    }

    @Test
    void buscarPorNumeroFactura_nulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> ventaService.buscarPorNumeroFactura(null));
    }

    @Test
    void eliminarVenta_marcaEliminadoFacturaYDetalles() {
        Detalle d1 = Detalle.builder().id("d1").eliminado(false).build();
        Detalle d2 = Detalle.builder().id("d2").eliminado(false).build();
        Set<Detalle> detalles = new HashSet<>(List.of(d1, d2));

        Factura f = Factura.builder()
                .id("fac-1")
                .numeroFactura(1001L)
                .eliminado(false)
                .detalles(detalles)
                .build();

        when(facturaRepository.findActive("fac-1")).thenReturn(Optional.of(f));
        when(facturaRepository.save(any(Factura.class))).thenAnswer(i -> i.getArgument(0));

        ventaService.eliminarVenta("fac-1");

        assertTrue(f.isEliminado());
        assertTrue(d1.isEliminado());
        assertTrue(d2.isEliminado());
        verify(facturaRepository).save(f);
    }

    @Test
    void buscarOrderDtoPorIdentificador_conNumeroFacturaConPrefijo_retornaDto() {
        Factura f = Factura.builder()
                .id("fac-1")
                .numeroFactura(1001L)
                .fechaFactura(LocalDateTime.now())
                .totalPagado(1500.0)
                .eliminado(false)
                .detalles(new HashSet<>())
                .build();

        when(facturaRepository.findByNumeroFacturaAndEliminadoFalse(1001L)).thenReturn(Optional.of(f));

        com.example.zero.dto.OrderViewDto dto = ventaService.buscarOrderDtoPorIdentificador("#ORD-1001");

        assertNotNull(dto);
        assertEquals("#ORD-1001", dto.getOrderNumber());
        assertEquals(1500.0, dto.getTotalAmount());
    }

    @Test
    void buscarOrderDtoPorIdentificador_noExistente_retornaNull() {
        when(facturaRepository.findByNumeroFacturaAndEliminadoFalse(9999L)).thenReturn(Optional.empty());
        when(facturaRepository.findActive("9999")).thenReturn(Optional.empty());

        com.example.zero.dto.OrderViewDto dto = ventaService.buscarOrderDtoPorIdentificador("#ORD-9999");

        assertNull(dto);
    }

    @Test
    void buscarOrderDtoPorIdentificador_nuloOVacio_retornaNull() {
        assertNull(ventaService.buscarOrderDtoPorIdentificador(null));
        assertNull(ventaService.buscarOrderDtoPorIdentificador("   "));
    }
}

