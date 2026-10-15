package com.example.zero.services;

import com.example.zero.entidades.compra.Factura;
import com.example.zero.entidades.compraCliente.DetalleCompra;
import com.example.zero.entidades.compraCliente.FacturaCliente;
import com.example.zero.entidades.compraCliente.OrdenCompra;
import com.example.zero.entidades.persona.Cliente;
import com.example.zero.entidades.producto.Producto;
import com.example.zero.enums.EstadoOrdenCompra;
import com.example.zero.repositories.FacturaRepository;
import com.example.zero.repositories.OrdenCompraRepository;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios - MercadoPagoService (Flujo de Pago con Mercado Pago)")
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

    @Mock
    private HttpSession session;

    @InjectMocks
    private MercadoPagoService mercadoPagoService;

    private Cliente clienteTest;
    private Producto productoTest;
    private OrdenCompra ordenTest;
    private DetalleCompra detalleTest;

    @BeforeEach
    void setUp() {
        clienteTest = Cliente.builder()
                .numeroDocumento("35999888")
                .nombre("Martin")
                .apellido("Ferrari")
                .eliminado(false)
                .build();

        productoTest = Producto.builder()
                .id("prod-mp-1")
                .codigo("PROD-MP-1")
                .nombre("Zapatillas Nitro Pro")
                .eliminado(false)
                .build();

        detalleTest = DetalleCompra.builder()
                .id("det-mp-1")
                .producto(productoTest)
                .cantidad(2)
                .precioUnitario(45000.0)
                .subtotal(90000.0)
                .eliminado(false)
                .build();

        List<DetalleCompra> detalles = new ArrayList<>();
        detalles.add(detalleTest);

        ordenTest = OrdenCompra.builder()
                .id("ord-mp-12345")
                .identificadorCompra("CART-12345")
                .estadoOrdenCompra(EstadoOrdenCompra.PENDIENTE_COMPLETAR)
                .cliente(clienteTest)
                .emailUsuario("martin.ferrari@zero.com")
                .total(90000.0)
                .detalles(detalles)
                .eliminado(false)
                .build();
    }

    @Nested
    @DisplayName("1. Inicialización y Configuración")
    class ConfiguracionTests {

        @Test
        @DisplayName("Debe configurar correctamente el access token en MercadoPagoConfig")
        void testInicializarConfiguracion() {
            mercadoPagoService.inicializarConfiguracion();
            assertNotNull(MercadoPagoConfig.getAccessToken(), "El access token no debe ser nulo");
            assertFalse(MercadoPagoConfig.getAccessToken().isBlank(), "El access token debe estar configurado");
        }
    }

    @Nested
    @DisplayName("2. Creación de Preferencia de Pago en Mercado Pago")
    class CreacionPreferenciaTests {

        @Test
        @DisplayName("Debe crear la preferencia de pago y retornar la URL de redirección (init_point)")
        void testCrearPreferenciaParaCarrito_Exitoso() throws Exception {
            when(ordenCompraService.obtenerItemsActivos(ordenTest)).thenReturn(List.of(detalleTest));
            when(stockService.calcularStockActual("prod-mp-1")).thenReturn(10); // Hay stock suficiente (10 >= 2)

            Preference preferenceMock = mock(Preference.class);
            when(preferenceMock.getInitPoint()).thenReturn("https://www.mercadopago.com.ar/checkout/v1/redirect?pref_id=123");

            try (MockedConstruction<PreferenceClient> mocked = mockConstruction(
                    PreferenceClient.class,
                    (mock, context) -> when(mock.create(any(PreferenceRequest.class))).thenReturn(preferenceMock)
            )) {
                String urlRedireccion = mercadoPagoService.crearPreferenciaParaCarrito(ordenTest, clienteTest, "http://localhost:8080");

                assertNotNull(urlRedireccion, "La URL de redirección no debe ser nula");
                assertEquals("https://www.mercadopago.com.ar/checkout/v1/redirect?pref_id=123", urlRedireccion);
                assertEquals(1, mocked.constructed().size(), "Debe haberse instanciado PreferenceClient");
            }
        }

        @Test
        @DisplayName("Debe retornar sandbox_init_point si init_point es nulo o vacío")
        void testCrearPreferenciaParaCarrito_SandboxFallback() throws Exception {
            when(ordenCompraService.obtenerItemsActivos(ordenTest)).thenReturn(List.of(detalleTest));
            when(stockService.calcularStockActual("prod-mp-1")).thenReturn(5);

            Preference preferenceMock = mock(Preference.class);
            when(preferenceMock.getInitPoint()).thenReturn(null);
            when(preferenceMock.getSandboxInitPoint()).thenReturn("https://sandbox.mercadopago.com.ar/checkout/v1/redirect?pref_id=sandbox-123");

            try (MockedConstruction<PreferenceClient> mocked = mockConstruction(
                    PreferenceClient.class,
                    (mock, context) -> when(mock.create(any(PreferenceRequest.class))).thenReturn(preferenceMock)
            )) {
                String urlRedireccion = mercadoPagoService.crearPreferenciaParaCarrito(ordenTest, clienteTest, "http://localhost:8080");

                assertEquals("https://sandbox.mercadopago.com.ar/checkout/v1/redirect?pref_id=sandbox-123", urlRedireccion);
            }
        }

        @Test
        @DisplayName("Debe lanzar IllegalStateException si la orden no contiene ítems activos para abonar")
        void testCrearPreferenciaParaCarrito_CarritoVacio_LanzaExcepcion() {
            when(ordenCompraService.obtenerItemsActivos(ordenTest)).thenReturn(List.of());

            IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                    mercadoPagoService.crearPreferenciaParaCarrito(ordenTest, clienteTest, "http://localhost:8080")
            );

            assertTrue(ex.getMessage().contains("no contiene productos para abonar"));
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException si algún producto del carrito ya no cuenta con stock disponible")
        void testCrearPreferenciaParaCarrito_StockInsuficiente_LanzaExcepcion() {
            when(ordenCompraService.obtenerItemsActivos(ordenTest)).thenReturn(List.of(detalleTest));
            when(stockService.calcularStockActual("prod-mp-1")).thenReturn(1); // Requiere 2, solo hay 1

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    mercadoPagoService.crearPreferenciaParaCarrito(ordenTest, clienteTest, "http://localhost:8080")
            );

            assertTrue(ex.getMessage().contains("Stock insuficiente"));
        }
    }

    @Nested
    @DisplayName("3. Procesamiento de Pago Exitoso (Confirmación y Transición de Estado)")
    class ProcesamientoPagoExitosoTests {

        private FacturaCliente facturaMock;

        @BeforeEach
        void setUpFactura() {
            facturaMock = FacturaCliente.builder()
                    .id("fac-1001")
                    .numeroFactura(1001L)
                    .totalPagado(90000.0)
                    .ordenCompra(ordenTest)
                    .eliminado(false)
                    .build();
        }

        @Test
        @DisplayName("Debe registrar la venta formal con BILLETERA_VIRTUAL, cambiar estado de orden a PENDIENTE_ENVIO y guardar")
        void testProcesarPagoExitoso_Exitoso_TransicionaAPendienteEnvioYRegistraVenta() {
            when(ordenCompraRepository.findById("ord-mp-12345")).thenReturn(Optional.of(ordenTest));
            when(ordenCompraService.obtenerItemsActivos(ordenTest)).thenReturn(List.of(detalleTest));

            when(ventaService.registrarVenta(
                    eq("35999888"),
                    eq("Martin"),
                    eq("Ferrari"),
                    eq("martin.ferrari@zero.com"),
                    eq("BILLETERA_VIRTUAL"),
                    eq(List.of("prod-mp-1")),
                    eq(List.of(2))
            )).thenReturn(facturaMock);

            when(ordenCompraRepository.save(any(OrdenCompra.class))).thenAnswer(i -> i.getArgument(0));

            Factura factura = mercadoPagoService.procesarPagoExitoso("ord-mp-12345", "pay-9999", clienteTest);

            assertNotNull(factura, "Debe retornar la factura generada");
            assertEquals(1001L, factura.getNumeroFactura());

            assertEquals(EstadoOrdenCompra.PENDIENTE_ENVIO, ordenTest.getEstadoOrdenCompra(),
                    "La orden de compra debe pasar de PENDIENTE_COMPLETAR a PENDIENTE_ENVIO tras el pago exitoso");

            verify(ventaService, times(1)).registrarVenta(
                    eq("35999888"),
                    eq("Martin"),
                    eq("Ferrari"),
                    eq("martin.ferrari@zero.com"),
                    eq("BILLETERA_VIRTUAL"),
                    eq(List.of("prod-mp-1")),
                    eq(List.of(2))
            );

            verify(ordenCompraRepository, times(1)).save(ordenTest);
        }

        @Test
        @DisplayName("Debe localizar la orden por cliente si externalReference es nula o vacía")
        void testProcesarPagoExitoso_BusquedaPorCliente_SiExternalReferenceEsNulo() {
            when(ordenCompraRepository.findByClienteAndEstadoOrdenCompraAndEliminadoFalse(
                    clienteTest, EstadoOrdenCompra.PENDIENTE_COMPLETAR
            )).thenReturn(Optional.of(ordenTest));

            when(ordenCompraService.obtenerItemsActivos(ordenTest)).thenReturn(List.of(detalleTest));
            when(ventaService.registrarVenta(anyString(), anyString(), anyString(), anyString(), anyString(), anyList(), anyList()))
                    .thenReturn(facturaMock);
            when(ordenCompraRepository.save(any(OrdenCompra.class))).thenAnswer(i -> i.getArgument(0));

            Factura factura = mercadoPagoService.procesarPagoExitoso(null, "pay-123", clienteTest);

            assertNotNull(factura);
            assertEquals(EstadoOrdenCompra.PENDIENTE_ENVIO, ordenTest.getEstadoOrdenCompra());
            verify(ordenCompraRepository, times(1)).save(ordenTest);
        }

        @Test
        @DisplayName("Idempotencia: Si la orden ya fue pagada (no está en PENDIENTE_COMPLETAR), no debe duplicar la venta")
        void testProcesarPagoExitoso_Idempotencia_OrdenYaCompletada() {
            // Simular que la orden ya fue completada previamente (ej. por webhook anterior)
            ordenTest.setEstadoOrdenCompra(EstadoOrdenCompra.PENDIENTE_ENVIO);

            when(ordenCompraRepository.findById("ord-mp-12345")).thenReturn(Optional.of(ordenTest));
            when(facturaRepository.findByEliminadoFalseOrderByFechaFacturaDesc()).thenReturn(List.of(facturaMock));

            Factura resultado = mercadoPagoService.procesarPagoExitoso("ord-mp-12345", "pay-9999", clienteTest);

            assertNotNull(resultado);
            assertSame(facturaMock, resultado);

            // NO debe volver a llamar a registrarVenta para evitar facturas duplicadas
            verify(ventaService, never()).registrarVenta(anyString(), anyString(), anyString(), anyString(), anyString(), anyList(), anyList());
        }

        @Test
        @DisplayName("Debe lanzar IllegalStateException si la orden de compra no es encontrada")
        void testProcesarPagoExitoso_OrdenNoEncontrada_LanzaExcepcion() {
            when(ordenCompraRepository.findById("ord-inexistente")).thenReturn(Optional.empty());
            when(ordenCompraRepository.findByClienteAndEstadoOrdenCompraAndEliminadoFalse(any(), any()))
                    .thenReturn(Optional.empty());
            when(facturaRepository.findByEliminadoFalseOrderByFechaFacturaDesc()).thenReturn(List.of());

            IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                    mercadoPagoService.procesarPagoExitoso("ord-inexistente", "pay-1", clienteTest)
            );

            assertTrue(ex.getMessage().contains("No se encontró la orden de compra activa"));
        }

        @Test
        @DisplayName("Debe lanzar IllegalStateException si la orden de compra encontrada no posee ítems activos")
        void testProcesarPagoExitoso_OrdenSinItemsActivos_LanzaExcepcion() {
            when(ordenCompraRepository.findById("ord-mp-12345")).thenReturn(Optional.of(ordenTest));
            when(ordenCompraService.obtenerItemsActivos(ordenTest)).thenReturn(List.of());

            IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                    mercadoPagoService.procesarPagoExitoso("ord-mp-12345", "pay-1", clienteTest)
            );

            assertTrue(ex.getMessage().contains("no contiene ítems activos"));
            verify(ventaService, never()).registrarVenta(anyString(), anyString(), anyString(), anyString(), anyString(), anyList(), anyList());
        }

        @Test
        @DisplayName("Debe lanzar IllegalStateException si la orden no tiene cliente asociado")
        void testProcesarPagoExitoso_OrdenSinCliente_LanzaExcepcion() {
            ordenTest.setCliente(null);
            when(ordenCompraRepository.findById("ord-mp-12345")).thenReturn(Optional.of(ordenTest));

            IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                    mercadoPagoService.procesarPagoExitoso("ord-mp-12345", "pay-1", null)
            );

            assertTrue(ex.getMessage().contains("no tiene un cliente asociado"));
        }
    }
}