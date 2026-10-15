package com.example.zero.services;

import com.example.zero.entidades.compraCliente.DetalleCompra;
import com.example.zero.entidades.compraCliente.OrdenCompra;
import com.example.zero.entidades.persona.Cliente;
import com.example.zero.entidades.persona.Usuario;
import com.example.zero.entidades.producto.Producto;
import com.example.zero.enums.EstadoOrdenCompra;
import com.example.zero.enums.RolUsuario;
import com.example.zero.repositories.*;
import com.example.zero.services.producto.ProductoService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios - OrdenCompraService (Caso de Uso: Generar Orden de Compra)")
class OrdenCompraServiceTest {

    @Mock
    private OrdenCompraRepository ordenCompraRepository;

    @Mock
    private DetalleCompraRepository detalleCompraRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ProductoService productoService;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private NacionalidadRepository nacionalidadRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private StockService stockService;

    @Mock
    private HttpSession session;

    @InjectMocks
    private OrdenCompraService ordenCompraService;

    private Cliente clienteTest;
    private Producto productoTest;
    private Usuario usuarioCliente;

    @BeforeEach
    void setUp() {
        clienteTest = Cliente.builder()
                .numeroDocumento("35123456")
                .nombre("Martin")
                .apellido("Ferrari")
                .eliminado(false)
                .build();

        productoTest = Producto.builder()
                .id("prod-100")
                .codigo("PROD-100")
                .nombre("Remera Zero Sport")
                .eliminado(false)
                .build();

        usuarioCliente = Usuario.builder()
                .id("usr-1")
                .nombreUsuario("martin@zero.com")
                .rol(RolUsuario.CLIENTE)
                .persona(clienteTest)
                .eliminado(false)
                .build();
        lenient().when(session.getAttribute("usuariosession")).thenReturn(usuarioCliente);
        //lenient() hace stubbing sobre el session del usuario. Stubbing es simular la respuesta de un método, en este caso para que devuelva usuarioCLiente y no choque con email nulo
    }

    @Nested
    @DisplayName("1. Alta / Generación de Orden de Compra")
    class GeneracionOrdenCompraTests {

        @Test
        @DisplayName("Debe crear una nueva orden de compra en estado PENDIENTE_COMPLETAR si no existe carrito activo")
        void testObtenerOCrearCarrito_CuandoNoExiste_CreaNuevoCarritoPendienteCompletar() {
            when(ordenCompraRepository.findByClienteAndEstadoOrdenCompraAndEliminadoFalse(
                    clienteTest, EstadoOrdenCompra.PENDIENTE_COMPLETAR
            )).thenReturn(Optional.empty());

            when(ordenCompraRepository.save(any(OrdenCompra.class))) //si guardó alguna orden de compra
                    .thenAnswer(invocation -> invocation.getArgument(0));//entonces se deja constancia de que se hizo una vez

            OrdenCompra orden = ordenCompraService.obtenerOCrearCarrito(clienteTest);

            assertNotNull(orden, "La orden generada no debe ser nula");
            assertEquals(EstadoOrdenCompra.PENDIENTE_COMPLETAR, orden.getEstadoOrdenCompra(),
                    "La orden de compra inicial debe crearse en estado PENDIENTE_COMPLETAR");
            assertEquals(0.0, orden.getTotal(), 0.001, "El total inicial de la orden debe ser 0.0");
            assertEquals(clienteTest, orden.getCliente(), "La orden debe estar asociada al cliente solicitante");
            assertEquals("martin@zero.com", orden.getEmailUsuario(), "La orden debe asociar el email del cliente autenticado");
            assertTrue(orden.getIdentificadorCompra().startsWith("CART-"), "El identificador debe tener prefijo CART-");
            assertNotNull(orden.getDetalles(), "La lista de detalles debe inicializarse vacía");
            assertTrue(orden.getDetalles().isEmpty(), "No debe tener detalles al inicio");

            verify(ordenCompraRepository, times(1)).save(any(OrdenCompra.class)); //verifica que el save de orden compra repository se haya llamado una vez
        }

        @Test
        @DisplayName("Debe retornar la orden existente si ya tiene una en PENDIENTE_COMPLETAR y no crear una nueva")
        void testObtenerOCrearCarrito_CuandoYaExiste_RetornaCarritoExistente() {
            OrdenCompra ordenExistente = OrdenCompra.builder()
                    .id("ord-existente-1")
                    .identificadorCompra("CART-ABCD1234")
                    .estadoOrdenCompra(EstadoOrdenCompra.PENDIENTE_COMPLETAR)
                    .cliente(clienteTest)
                    .total(150.0)
                    .detalles(new ArrayList<>())
                    .build();

            when(ordenCompraRepository.findByClienteAndEstadoOrdenCompraAndEliminadoFalse(
                    clienteTest, EstadoOrdenCompra.PENDIENTE_COMPLETAR
            )).thenReturn(Optional.of(ordenExistente));

            OrdenCompra resultado = ordenCompraService.obtenerOCrearCarrito(clienteTest);

            assertSame(ordenExistente, resultado, "Debe retornar la misma instancia existente");
            verify(ordenCompraRepository, never()).save(any(OrdenCompra.class));
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException si el cliente es nulo al obtener o crear carrito")
        void testObtenerOCrearCarrito_ClienteNulo_LanzaExcepcion() {
            assertThrows(IllegalArgumentException.class, () -> ordenCompraService.obtenerOCrearCarrito(null));
        }

        @Test
        @DisplayName("Al agregar el primer producto debe generar la orden en PENDIENTE_COMPLETAR y asociar el detalle")
        void testAgregarPrimerProducto_CreaOrdenYDetalleCorrectamente() {
            when(productoRepository.findActive("prod-100")).thenReturn(Optional.of(productoTest));
            when(productoService.obtenerPrecioActual("prod-100")).thenReturn(1500.0);
            when(stockService.calcularStockActual("prod-100")).thenReturn(10);

            when(ordenCompraRepository.findByClienteAndEstadoOrdenCompraAndEliminadoFalse(
                    clienteTest, EstadoOrdenCompra.PENDIENTE_COMPLETAR
            )).thenReturn(Optional.empty());

            when(ordenCompraRepository.save(any(OrdenCompra.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            when(detalleCompraRepository.save(any(DetalleCompra.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            OrdenCompra orden = ordenCompraService.agregarProducto(clienteTest, "prod-100", 2);

            assertNotNull(orden);
            assertEquals(EstadoOrdenCompra.PENDIENTE_COMPLETAR, orden.getEstadoOrdenCompra());
            assertEquals(3000.0, orden.getTotal(), 0.001, "El total debe ser 2 * 1500.0 = 3000.0");
            assertEquals(1, orden.getDetalles().size(), "Debe contener 1 detalle de compra");

            DetalleCompra det = orden.getDetalles().get(0);
            assertEquals(productoTest, det.getProducto());
            assertEquals(2, det.getCantidad());
            assertEquals(1500.0, det.getPrecioUnitario());
            assertEquals(3000.0, det.getSubtotal());

            verify(detalleCompraRepository, times(1)).save(any(DetalleCompra.class));
            verify(ordenCompraRepository, atLeastOnce()).save(any(OrdenCompra.class));
        }

        @Test
        @DisplayName("Al agregar un producto existente en la orden debe incrementar su cantidad y recalcular el total")
        void testAgregarProducto_ProductoExistente_IncrementaCantidadYActualizaTotal() {
            DetalleCompra detallePrevio = DetalleCompra.builder()
                    .id("det-1")
                    .producto(productoTest)
                    .cantidad(2)
                    .precioUnitario(1500.0)
                    .subtotal(3000.0)
                    .eliminado(false)
                    .build();

            List<DetalleCompra> listaDetalles = new ArrayList<>();
            listaDetalles.add(detallePrevio);

            OrdenCompra ordenExistente = OrdenCompra.builder()
                    .id("ord-1")
                    .estadoOrdenCompra(EstadoOrdenCompra.PENDIENTE_COMPLETAR)
                    .cliente(clienteTest)
                    .total(3000.0)
                    .detalles(listaDetalles)
                    .build();

            when(productoRepository.findActive("prod-100")).thenReturn(Optional.of(productoTest));
            when(productoService.obtenerPrecioActual("prod-100")).thenReturn(1500.0);
            when(stockService.calcularStockActual("prod-100")).thenReturn(10); // Stock suficiente para 2 + 3 = 5

            when(ordenCompraRepository.findByClienteAndEstadoOrdenCompraAndEliminadoFalse(
                    clienteTest, EstadoOrdenCompra.PENDIENTE_COMPLETAR
            )).thenReturn(Optional.of(ordenExistente));

            when(detalleCompraRepository.save(any(DetalleCompra.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(ordenCompraRepository.save(any(OrdenCompra.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            OrdenCompra orden = ordenCompraService.agregarProducto(clienteTest, "prod-100", 3);

            assertEquals(1, orden.getDetalles().size(), "No debe agregar un nuevo detalle sino actualizar el existente");
            assertEquals(5, detallePrevio.getCantidad(), "La cantidad debe ser 2 + 3 = 5");
            assertEquals(7500.0, detallePrevio.getSubtotal(), 0.001, "El subtotal debe ser 5 * 1500.0 = 7500.0");
            assertEquals(7500.0, orden.getTotal(), 0.001, "El total de la orden debe actualizarse a 7500.0");
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException si no hay stock suficiente para la cantidad solicitada")
        void testAgregarProducto_StockInsuficiente_LanzaExcepcion() {
            when(productoRepository.findActive("prod-100")).thenReturn(Optional.of(productoTest));
            when(productoService.obtenerPrecioActual("prod-100")).thenReturn(1500.0);
            when(stockService.calcularStockActual("prod-100")).thenReturn(3); // Solo hay 3 disponibles

            OrdenCompra ordenVacia = OrdenCompra.builder()
                    .estadoOrdenCompra(EstadoOrdenCompra.PENDIENTE_COMPLETAR)
                    .cliente(clienteTest)
                    .detalles(new ArrayList<>())
                    .build();

            when(ordenCompraRepository.findByClienteAndEstadoOrdenCompraAndEliminadoFalse(
                    clienteTest, EstadoOrdenCompra.PENDIENTE_COMPLETAR
            )).thenReturn(Optional.of(ordenVacia));

            // Se intentan agregar 5 unidades (supera el stock de 3)
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    ordenCompraService.agregarProducto(clienteTest, "prod-100", 5)
            );

            assertTrue(ex.getMessage().contains("Stock insuficiente"),
                    "El mensaje de excepción debe avisar que el stock es insuficiente");
            verify(detalleCompraRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException al intentar agregar cantidad menor o igual a cero")
        void testAgregarProducto_CantidadInvalida_LanzaExcepcion() {
            assertThrows(IllegalArgumentException.class, () ->
                    ordenCompraService.agregarProducto(clienteTest, "prod-100", 0)
            );
            assertThrows(IllegalArgumentException.class, () ->
                    ordenCompraService.agregarProducto(clienteTest, "prod-100", -2)
            );
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException si el producto no existe o está inactivo")
        void testAgregarProducto_ProductoInexistente_LanzaExcepcion() {
            when(productoRepository.findActive("prod-fantasma")).thenReturn(Optional.empty());
            when(productoRepository.findByCodigoAndEliminadoFalse("prod-fantasma")).thenReturn(Optional.empty());
            when(productoRepository.findByEliminadoFalse()).thenReturn(List.of());

            assertThrows(IllegalArgumentException.class, () ->
                    ordenCompraService.agregarProducto(clienteTest, "prod-fantasma", 1)
            );
        }
    }

    @Nested
    @DisplayName("2. Modificación de Orden de Compra (actualizar cantidades)")
    class ModificacionOrdenCompraTests {

        private OrdenCompra ordenConItems;
        private DetalleCompra detalle;

        @BeforeEach
        void setUpItem() {
            detalle = DetalleCompra.builder()
                    .id("det-55")
                    .producto(productoTest)
                    .cantidad(2)
                    .precioUnitario(1000.0)
                    .subtotal(2000.0)
                    .eliminado(false)
                    .build();

            List<DetalleCompra> detalles = new ArrayList<>();
            detalles.add(detalle);

            ordenConItems = OrdenCompra.builder()
                    .id("ord-mod")
                    .estadoOrdenCompra(EstadoOrdenCompra.PENDIENTE_COMPLETAR)
                    .cliente(clienteTest)
                    .total(2000.0)
                    .detalles(detalles)
                    .build();
        }

        @Test
        @DisplayName("Debe actualizar la cantidad del ítem, recalcular su subtotal y el total de la orden")
        void testActualizarCantidad_Exitoso() {
            when(ordenCompraRepository.findByClienteAndEstadoOrdenCompraAndEliminadoFalse(
                    clienteTest, EstadoOrdenCompra.PENDIENTE_COMPLETAR
            )).thenReturn(Optional.of(ordenConItems));

            when(stockService.calcularStockActual("prod-100")).thenReturn(15);
            when(detalleCompraRepository.save(any(DetalleCompra.class))).thenAnswer(i -> i.getArgument(0));
            when(ordenCompraRepository.save(any(OrdenCompra.class))).thenAnswer(i -> i.getArgument(0));

            OrdenCompra orden = ordenCompraService.actualizarCantidad(clienteTest, "det-55", 6);

            assertEquals(6, detalle.getCantidad(), "La cantidad del ítem debe modificarse a 6");
            assertEquals(6000.0, detalle.getSubtotal(), 0.001, "El subtotal debe ser 6 * 1000 = 6000");
            assertEquals(6000.0, orden.getTotal(), 0.001, "El total de la orden debe actualizarse a 6000");
            verify(detalleCompraRepository, times(1)).save(detalle);
            verify(ordenCompraRepository, times(1)).save(ordenConItems);
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException al actualizar si la nueva cantidad supera el stock")
        void testActualizarCantidad_StockInsuficiente_LanzaExcepcion() {
            when(ordenCompraRepository.findByClienteAndEstadoOrdenCompraAndEliminadoFalse(
                    clienteTest, EstadoOrdenCompra.PENDIENTE_COMPLETAR
            )).thenReturn(Optional.of(ordenConItems));

            when(stockService.calcularStockActual("prod-100")).thenReturn(4);

            // Intentar subir de 2 a 10 cuando solo hay 4
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    ordenCompraService.actualizarCantidad(clienteTest, "det-55", 10)
            );

            assertTrue(ex.getMessage().contains("Stock insuficiente"));
            assertEquals(2, detalle.getCantidad(), "La cantidad no debe modificarse si la validación falló");
            verify(detalleCompraRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException si la cantidad a actualizar es menor o igual a cero")
        void testActualizarCantidad_CantidadMenorOIgualACero_LanzaExcepcion() {
            assertThrows(IllegalArgumentException.class, () ->
                    ordenCompraService.actualizarCantidad(clienteTest, "det-55", 0)
            );
            assertThrows(IllegalArgumentException.class, () ->
                    ordenCompraService.actualizarCantidad(clienteTest, "det-55", -1)
            );
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException si el detalleId no se encuentra en el carrito")
        void testActualizarCantidad_ItemNoExisteEnOrden_LanzaExcepcion() {
            when(ordenCompraRepository.findByClienteAndEstadoOrdenCompraAndEliminadoFalse(
                    clienteTest, EstadoOrdenCompra.PENDIENTE_COMPLETAR
            )).thenReturn(Optional.of(ordenConItems));

            assertThrows(IllegalArgumentException.class, () ->
                    ordenCompraService.actualizarCantidad(clienteTest, "det-inexistente", 3)
            );
        }
    }

    @Nested
    @DisplayName("3. Eliminación de Ítems y Vaciar Carrito (Baja)")
    class EliminacionOrdenCompraTests {

        private OrdenCompra ordenConDosItems;
        private DetalleCompra det1;
        private DetalleCompra det2;

        @BeforeEach
        void setUpItems() {
            det1 = DetalleCompra.builder()
                    .id("item-1")
                    .producto(productoTest)
                    .cantidad(2)
                    .precioUnitario(1000.0)
                    .subtotal(2000.0)
                    .eliminado(false)
                    .build();

            Producto prod2 = Producto.builder().id("prod-200").nombre("Medias").build();
            det2 = DetalleCompra.builder()
                    .id("item-2")
                    .producto(prod2)
                    .cantidad(1)
                    .precioUnitario(500.0)
                    .subtotal(500.0)
                    .eliminado(false)
                    .build();

            List<DetalleCompra> lista = new ArrayList<>();
            lista.add(det1);
            lista.add(det2);

            ordenConDosItems = OrdenCompra.builder()
                    .id("ord-del")
                    .estadoOrdenCompra(EstadoOrdenCompra.PENDIENTE_COMPLETAR)
                    .cliente(clienteTest)
                    .total(2500.0)
                    .detalles(lista)
                    .build();
        }

        @Test
        @DisplayName("Debe marcar el ítem como eliminado (borrado lógico) y restar su valor del total de la orden")
        void testEliminarProducto_Exitoso() {
            when(ordenCompraRepository.findByClienteAndEstadoOrdenCompraAndEliminadoFalse(
                    clienteTest, EstadoOrdenCompra.PENDIENTE_COMPLETAR
            )).thenReturn(Optional.of(ordenConDosItems));

            when(detalleCompraRepository.save(any(DetalleCompra.class))).thenAnswer(i -> i.getArgument(0));
            when(ordenCompraRepository.save(any(OrdenCompra.class))).thenAnswer(i -> i.getArgument(0));

            OrdenCompra orden = ordenCompraService.eliminarProducto(clienteTest, "item-1");

            assertTrue(det1.isEliminado(), "El ítem debe quedar marcado como eliminado lógicamente");
            assertFalse(det2.isEliminado(), "El otro ítem no debe ser afectado");
            assertEquals(500.0, orden.getTotal(), 0.001, "El nuevo total debe excluir el ítem eliminado (2500 - 2000 = 500)");

            verify(detalleCompraRepository, times(1)).save(det1);
            verify(ordenCompraRepository, times(1)).save(ordenConDosItems);
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException al intentar eliminar un ítem inexistente")
        void testEliminarProducto_ItemNoExiste_LanzaExcepcion() {
            when(ordenCompraRepository.findByClienteAndEstadoOrdenCompraAndEliminadoFalse(
                    clienteTest, EstadoOrdenCompra.PENDIENTE_COMPLETAR
            )).thenReturn(Optional.of(ordenConDosItems));

            assertThrows(IllegalArgumentException.class, () ->
                    ordenCompraService.eliminarProducto(clienteTest, "item-fantasma")
            );
            verify(detalleCompraRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe vaciar todos los ítems activos marcándolos como eliminados y dejar total en 0.0")
        void testVaciarCarrito_Exitoso() {
            when(ordenCompraRepository.findByClienteAndEstadoOrdenCompraAndEliminadoFalse(
                    clienteTest, EstadoOrdenCompra.PENDIENTE_COMPLETAR
            )).thenReturn(Optional.of(ordenConDosItems));

            when(detalleCompraRepository.save(any(DetalleCompra.class))).thenAnswer(i -> i.getArgument(0));
            when(ordenCompraRepository.save(any(OrdenCompra.class))).thenAnswer(i -> i.getArgument(0));

            OrdenCompra orden = ordenCompraService.vaciarCarrito(clienteTest);

            assertTrue(det1.isEliminado(), "Todos los ítems deben quedar con eliminado=true");
            assertTrue(det2.isEliminado(), "Todos los ítems deben quedar con eliminado=true");
            assertEquals(0.0, orden.getTotal(), 0.001, "El total del carrito vacío debe ser 0.0");

            verify(detalleCompraRepository, times(2)).save(any(DetalleCompra.class));
            verify(ordenCompraRepository, times(1)).save(ordenConDosItems);
        }
    }

    @Nested
    @DisplayName("4. Reglas de Cálculo y Métodos Utilitarios")
    class UtilitariosLógicaNegocioTests {

        @Test
        @DisplayName("recalcularTotal debe excluir los ítems marcados como eliminados")
        void testRecalcularTotal_IgnoraDetallesEliminados() {
            DetalleCompra activo = DetalleCompra.builder().subtotal(1200.0).eliminado(false).build();
            DetalleCompra borrado = DetalleCompra.builder().subtotal(800.0).eliminado(true).build();

            OrdenCompra orden = OrdenCompra.builder()
                    .detalles(List.of(activo, borrado))
                    .build();

            ordenCompraService.recalcularTotal(orden);

            assertEquals(1200.0, orden.getTotal(), 0.001, "Solo debe sumar el ítem activo");
        }

        @Test
        @DisplayName("contarItems debe sumar las unidades físicas únicamente de ítems activos")
        void testContarItems_SumaCantidadesDeItemsActivos() {
            DetalleCompra item1 = DetalleCompra.builder().cantidad(3).eliminado(false).build();
            DetalleCompra item2 = DetalleCompra.builder().cantidad(2).eliminado(false).build();
            DetalleCompra itemEliminado = DetalleCompra.builder().cantidad(5).eliminado(true).build();

            OrdenCompra orden = OrdenCompra.builder()
                    .detalles(List.of(item1, item2, itemEliminado))
                    .build();

            int conteo = ordenCompraService.contarItems(orden);

            assertEquals(5, conteo, "El conteo debe ser 3 + 2 = 5, ignorando el ítem eliminado");
        }

        @Test
        @DisplayName("obtenerItemsActivos debe retornar solo los detalles con eliminado=false")
        void testObtenerItemsActivos_FiltraSoloNoEliminados() {
            DetalleCompra d1 = DetalleCompra.builder().id("1").eliminado(false).build();
            DetalleCompra d2 = DetalleCompra.builder().id("2").eliminado(true).build();
            DetalleCompra d3 = DetalleCompra.builder().id("3").eliminado(false).build();

            OrdenCompra orden = OrdenCompra.builder().detalles(List.of(d1, d2, d3)).build();

            List<DetalleCompra> activos = ordenCompraService.obtenerItemsActivos(orden);

            assertEquals(2, activos.size());
            assertTrue(activos.contains(d1));
            assertTrue(activos.contains(d3));
            assertFalse(activos.contains(d2));
        }

        @Test
        @DisplayName("obtenerOAsociarCliente debe retornar el Cliente si el usuario tiene rol CLIENTE")
        void testObtenerOAsociarCliente_UsuarioValido() {
            when(usuarioRepository.findById("usr-1")).thenReturn(Optional.of(usuarioCliente));

            Cliente cliente = ordenCompraService.obtenerOAsociarCliente(usuarioCliente);

            assertNotNull(cliente);
            assertEquals(clienteTest, cliente);
        }

        @Test
        @DisplayName("obtenerOAsociarCliente debe rechazar usuarios con rol no CLIENTE")
        void testObtenerOAsociarCliente_UsuarioAdmin_LanzaExcepcion() {
            Usuario usuarioAdmin = Usuario.builder()
                    .id("adm-1")
                    .nombreUsuario("admin@zero.com")
                    .rol(RolUsuario.ADMINISTRATIVO)
                    .build();

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    ordenCompraService.obtenerOAsociarCliente(usuarioAdmin)
            );
            assertTrue(ex.getMessage().contains("Solo los usuarios con rol CLIENTE"));
        }
    }
}