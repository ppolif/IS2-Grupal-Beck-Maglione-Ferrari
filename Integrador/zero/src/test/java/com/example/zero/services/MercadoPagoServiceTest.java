package com.example.zero.services;

import com.example.zero.entidades.compra.Factura;
import com.example.zero.entidades.compraCliente.DetalleCompra;
import com.example.zero.entidades.compraCliente.OrdenCompra;
import com.example.zero.entidades.persona.Cliente;
import com.example.zero.entidades.persona.Usuario;
import com.example.zero.entidades.producto.Producto;
import com.example.zero.enums.EstadoOrdenCompra;
import com.example.zero.repositories.FacturaRepository;
import com.example.zero.repositories.OrdenCompraRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MercadoPagoServiceTest {

    @Mock
    private OrdenCompraService ordenCompraService;

    @Mock
    private OrdenCompraRepository ordenCompraRepository;

    @Mock
    private VentaService ventaService;

    @Mock
    private FacturaRepository facturaRepository;

    @Mock
    private StockService stockService;

    @InjectMocks
    private MercadoPagoService mercadoPagoService;

    @Test
    @DisplayName("crearPreferenciaParaCarrito con carrito vacío lanza IllegalStateException")
    void crearPreferenciaParaCarrito_conCarritoVacio_lanzaIllegalStateException() {
        OrdenCompra carrito = new OrdenCompra();
        Cliente cliente = new Cliente();
        when(ordenCompraService.obtenerItemsActivos(carrito)).thenReturn(Collections.emptyList());

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                mercadoPagoService.crearPreferenciaParaCarrito(carrito, cliente, "http://localhost:8080"));

        assertEquals("El carrito de compras no contiene productos para abonar.", ex.getMessage());
    }

    @Test
    @DisplayName("crearPreferenciaParaCarrito con stock insuficiente lanza IllegalArgumentException preventiva")
    void crearPreferenciaParaCarrito_conStockInsuficiente_lanzaIllegalArgumentException() {
        OrdenCompra carrito = new OrdenCompra();
        Cliente cliente = new Cliente();

        Producto producto = Producto.builder().id("p1").nombre("Buzo Zero").build();
        DetalleCompra detalle = DetalleCompra.builder()
                .id("d1")
                .producto(producto)
                .cantidad(4)
                .precioUnitario(5000.0)
                .build();

        when(ordenCompraService.obtenerItemsActivos(carrito)).thenReturn(List.of(detalle));
        when(stockService.calcularStockActual("p1")).thenReturn(2); // Solo hay 2, pide 4

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                mercadoPagoService.crearPreferenciaParaCarrito(carrito, cliente, "http://localhost:8080"));

        assertEquals("Stock insuficiente para: Buzo Zero", ex.getMessage());
    }

    @Test
    @DisplayName("procesarPagoExitoso registra venta en VentaService y actualiza estado a PENDIENTE_ENVIO")
    void procesarPagoExitoso_conOrdenValida_delegaEnVentaServiceYActualizaEstadoOrden() {
        Usuario usuario = Usuario.builder().id("u1").nombreUsuario("cliente@test.com").build();
        Cliente cliente = Cliente.builder()
                .numeroDocumento("40123456")
                .nombre("Martin")
                .apellido("Palermo")
                .usuario(usuario)
                .build();

        Producto producto = Producto.builder().id("prod-10").nombre("Botines Zero").build();
        DetalleCompra item = DetalleCompra.builder()
                .id("item-1")
                .producto(producto)
                .cantidad(2)
                .precioUnitario(12000.0)
                .subtotal(24000.0)
                .eliminado(false)
                .build();

        OrdenCompra orden = OrdenCompra.builder()
                .id("oc-1")
                .identificadorCompra("CART-1234")
                .cliente(cliente)
                .estadoOrdenCompra(EstadoOrdenCompra.PENDIENTE_COMPLETAR)
                .detalles(new ArrayList<>(List.of(item)))
                .build();

        when(ordenCompraRepository.findById("oc-1")).thenReturn(Optional.of(orden));
        when(ordenCompraService.obtenerItemsActivos(orden)).thenReturn(List.of(item));

        Factura factura = Factura.builder().id("fac-1").numeroFactura(2001L).build();
        when(ventaService.registrarVenta(
                eq("40123456"), eq("Martin"), eq("Palermo"), eq("cliente@test.com"),
                eq("BILLETERA_VIRTUAL"), eq(List.of("prod-10")), eq(List.of(2))
        )).thenReturn(factura);

        Factura resultado = mercadoPagoService.procesarPagoExitoso("oc-1", "pay-999", cliente);

        assertNotNull(resultado);
        assertEquals(2001L, resultado.getNumeroFactura());
        assertEquals(EstadoOrdenCompra.PENDIENTE_ENVIO, orden.getEstadoOrdenCompra());
        verify(ordenCompraRepository).save(orden);
        verify(ventaService).registrarVenta(
                eq("40123456"), eq("Martin"), eq("Palermo"), eq("cliente@test.com"),
                eq("BILLETERA_VIRTUAL"), eq(List.of("prod-10")), eq(List.of(2))
        );
    }

    @Test
    @DisplayName("procesarPagoExitoso sin orden activa lanza IllegalStateException")
    void procesarPagoExitoso_sinOrdenActiva_lanzaIllegalStateException() {
        Cliente cliente = Cliente.builder().numeroDocumento("40123456").build();
        when(ordenCompraRepository.findById("oc-inexistente")).thenReturn(Optional.empty());
        when(ordenCompraRepository.findByClienteAndEstadoOrdenCompraAndEliminadoFalse(cliente, EstadoOrdenCompra.PENDIENTE_COMPLETAR))
                .thenReturn(Optional.empty());
        when(facturaRepository.findByEliminadoFalseOrderByFechaFacturaDesc()).thenReturn(Collections.emptyList());

        assertThrows(IllegalStateException.class, () ->
                mercadoPagoService.procesarPagoExitoso("oc-inexistente", "pay-1", cliente));
    }
}

