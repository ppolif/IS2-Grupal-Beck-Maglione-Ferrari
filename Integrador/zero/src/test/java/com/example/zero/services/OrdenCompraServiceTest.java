package com.example.zero.services;

import com.example.zero.entidades.compraCliente.DetalleCompra;
import com.example.zero.entidades.compraCliente.OrdenCompra;
import com.example.zero.entidades.persona.Cliente;
import com.example.zero.entidades.persona.Nacionalidad;
import com.example.zero.entidades.persona.Usuario;
import com.example.zero.entidades.producto.Producto;
import com.example.zero.enums.EstadoOrdenCompra;
import com.example.zero.enums.RolUsuario;
import com.example.zero.enums.TipoDocumento;
import com.example.zero.repositories.*;
import com.example.zero.services.producto.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

    @InjectMocks
    private OrdenCompraService ordenCompraService;

    private Cliente clienteTest;
    private Producto productoTest;
    private OrdenCompra carritoTest;

    @BeforeEach
    void setUp() {
        Nacionalidad nac = Nacionalidad.builder().id("nac-01").nombre("Argentina").build();
        clienteTest = Cliente.builder()
                .numeroDocumento("12345678")
                .nombre("Juan")
                .apellido("Perez")
                .fechaNacimiento(LocalDate.of(1995, 5, 10))
                .tipoDocumento(TipoDocumento.DNI)
                .nacionalidad(nac)
                .eliminado(false)
                .build();

        productoTest = Producto.builder()
                .id("prod-uuid-1")
                .codigo("PROD-001")
                .nombre("Zapatilla Running")
                .precioActual(150.0)
                .eliminado(false)
                .build();

        carritoTest = OrdenCompra.builder()
                .id("cart-uuid-1")
                .identificadorCompra("CART-12345678")
                .cliente(clienteTest)
                .estadoOrdenCompra(EstadoOrdenCompra.PENDIENTE_COMPLETAR)
                .total(0.0)
                .eliminado(false)
                .detalles(new ArrayList<>())
                .build();
    }

    // ==================== MÉTODOS AUXILIARES Y CÁLCULOS ====================

    @Test
    @DisplayName("recalcularTotal calcula correctamente la suma de subtotales de detalles activos")
    void recalcularTotal_calculaCorrectamenteSumaDetallesActivos() {
        DetalleCompra d1 = DetalleCompra.builder().cantidad(2).precioUnitario(100.0).subtotal(200.0).eliminado(false).build();
        DetalleCompra d2 = DetalleCompra.builder().cantidad(1).precioUnitario(350.5).subtotal(350.5).eliminado(false).build();
        DetalleCompra dEliminado = DetalleCompra.builder().cantidad(1).precioUnitario(500.0).subtotal(500.0).eliminado(true).build();

        OrdenCompra orden = OrdenCompra.builder().detalles(new ArrayList<>(List.of(d1, d2, dEliminado))).build();

        ordenCompraService.recalcularTotal(orden);

        assertEquals(550.5, orden.getTotal());
    }

    @Test
    @DisplayName("recalcularSubtotal calcula cantidad * precio unitario")
    void recalcularSubtotal_calculaMultiplicacionCantidadYPrecio() {
        DetalleCompra detalle = DetalleCompra.builder().cantidad(3).precioUnitario(199.99).build();

        ordenCompraService.recalcularSubtotal(detalle);

        assertEquals(599.97, detalle.getSubtotal());
    }

    @Test
    @DisplayName("obtenerItemsActivos filtra los detalles eliminados")
    void obtenerItemsActivos_filtraCorrectamenteDetallesEliminados() {
        DetalleCompra d1 = DetalleCompra.builder().id("d1").eliminado(false).build();
        DetalleCompra d2 = DetalleCompra.builder().id("d2").eliminado(true).build();
        DetalleCompra d3 = DetalleCompra.builder().id("d3").eliminado(false).build();

        OrdenCompra orden = OrdenCompra.builder().detalles(List.of(d1, d2, d3)).build();

        List<DetalleCompra> activos = ordenCompraService.obtenerItemsActivos(orden);

        assertEquals(2, activos.size());
        assertTrue(activos.contains(d1));
        assertTrue(activos.contains(d3));
        assertFalse(activos.contains(d2));
    }

    @Test
    @DisplayName("contarItems cuenta solo cantidades de ítems no eliminados")
    void contarItems_cuentaCantidadesCorrectamente() {
        DetalleCompra item1 = DetalleCompra.builder().cantidad(3).eliminado(false).build();
        DetalleCompra item2 = DetalleCompra.builder().cantidad(2).eliminado(true).build();
        DetalleCompra item3 = DetalleCompra.builder().cantidad(4).eliminado(false).build();
        carritoTest.getDetalles().addAll(List.of(item1, item2, item3));

        int total = ordenCompraService.contarItems(carritoTest);

        assertEquals(7, total);
    }

    @Test
    @DisplayName("contarItems con orden nula retorna cero")
    void contarItems_ordenNula_retornaCero() {
        assertEquals(0, ordenCompraService.contarItems((OrdenCompra) null));
    }

    // ==================== OBTENER O CREAR CARRITO ====================

    @Test
    @DisplayName("obtenerOCrearCarrito retorna carrito existente si ya está en estado PENDIENTE_COMPLETAR")
    void obtenerOCrearCarrito_existente_retornaCarritoExistente() {
        when(ordenCompraRepository.findByClienteAndEstadoOrdenCompraAndEliminadoFalse(
                clienteTest, EstadoOrdenCompra.PENDIENTE_COMPLETAR))
                .thenReturn(Optional.of(carritoTest));

        OrdenCompra resultado = ordenCompraService.obtenerOCrearCarrito(clienteTest);

        assertNotNull(resultado);
        assertEquals(carritoTest.getId(), resultado.getId());
        verify(ordenCompraRepository, never()).save(any());
    }

    @Test
    @DisplayName("obtenerOCrearCarrito crea uno nuevo si no existe previo")
    void obtenerOCrearCarrito_inexistente_creaNuevoCarrito() {
        when(ordenCompraRepository.findByClienteAndEstadoOrdenCompraAndEliminadoFalse(
                clienteTest, EstadoOrdenCompra.PENDIENTE_COMPLETAR))
                .thenReturn(Optional.empty());
        when(ordenCompraRepository.save(any(OrdenCompra.class))).thenAnswer(inv -> inv.getArgument(0));

        OrdenCompra resultado = ordenCompraService.obtenerOCrearCarrito(clienteTest);

        assertNotNull(resultado);
        assertEquals(EstadoOrdenCompra.PENDIENTE_COMPLETAR, resultado.getEstadoOrdenCompra());
        assertEquals(clienteTest, resultado.getCliente());
        verify(ordenCompraRepository, times(1)).save(any(OrdenCompra.class));
    }

    @Test
    @DisplayName("obtenerOCrearCarrito con cliente nulo lanza IllegalArgumentException")
    void obtenerOCrearCarrito_clienteNulo_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> ordenCompraService.obtenerOCrearCarrito(null));
    }

    // ==================== AGREGAR PRODUCTO ====================

    @Test
    @DisplayName("agregarProducto agrega un nuevo ítem y recalcula total")
    void agregarProducto_productoNuevo_creaDetalleYRecalculaTotal() {
        when(productoRepository.findActive("prod-uuid-1")).thenReturn(Optional.of(productoTest));
        when(productoService.obtenerPrecioActual("prod-uuid-1")).thenReturn(150.0);
        when(ordenCompraRepository.findByClienteAndEstadoOrdenCompraAndEliminadoFalse(
                clienteTest, EstadoOrdenCompra.PENDIENTE_COMPLETAR))
                .thenReturn(Optional.of(carritoTest));
        when(detalleCompraRepository.save(any(DetalleCompra.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ordenCompraRepository.save(any(OrdenCompra.class))).thenAnswer(inv -> inv.getArgument(0));

        OrdenCompra resultado = ordenCompraService.agregarProducto(clienteTest, "prod-uuid-1", 2);

        assertNotNull(resultado);
        assertEquals(1, resultado.getDetalles().size());
        assertEquals(300.0, resultado.getTotal());
        verify(detalleCompraRepository, times(1)).save(any(DetalleCompra.class));
    }

    @Test
    @DisplayName("agregarProducto con producto ya existente en carrito acumula la cantidad")
    void agregarProducto_productoExistenteEnCarrito_incrementaCantidadYRecalculaTotal() {
        DetalleCompra itemExistente = DetalleCompra.builder()
                .id("det-1")
                .ordenCompra(carritoTest)
                .producto(productoTest)
                .cantidad(1)
                .precioUnitario(150.0)
                .subtotal(150.0)
                .eliminado(false)
                .build();
        carritoTest.getDetalles().add(itemExistente);
        carritoTest.setTotal(150.0);

        when(productoRepository.findActive("prod-uuid-1")).thenReturn(Optional.of(productoTest));
        when(productoService.obtenerPrecioActual("prod-uuid-1")).thenReturn(150.0);
        when(ordenCompraRepository.findByClienteAndEstadoOrdenCompraAndEliminadoFalse(
                clienteTest, EstadoOrdenCompra.PENDIENTE_COMPLETAR))
                .thenReturn(Optional.of(carritoTest));
        when(detalleCompraRepository.save(any(DetalleCompra.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ordenCompraRepository.save(any(OrdenCompra.class))).thenAnswer(inv -> inv.getArgument(0));

        OrdenCompra resultado = ordenCompraService.agregarProducto(clienteTest, "prod-uuid-1", 3);

        assertNotNull(resultado);
        assertEquals(1, resultado.getDetalles().size());
        assertEquals(4, itemExistente.getCantidad());
        assertEquals(600.0, itemExistente.getSubtotal());
        assertEquals(600.0, resultado.getTotal());
    }

    @Test
    @DisplayName("agregarProducto con cantidad <= 0 lanza IllegalArgumentException")
    void agregarProducto_cantidadInvalida_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                ordenCompraService.agregarProducto(clienteTest, "prod-uuid-1", 0));
        assertThrows(IllegalArgumentException.class, () ->
                ordenCompraService.agregarProducto(clienteTest, "prod-uuid-1", -1));
    }

    @Test
    @DisplayName("agregarProducto con producto inexistente lanza IllegalArgumentException")
    void agregarProducto_productoInexistente_lanzaIllegalArgumentException() {
        when(productoRepository.findActive("prod-inexistente")).thenReturn(Optional.empty());
        when(productoRepository.findByCodigoAndEliminadoFalse("prod-inexistente")).thenReturn(Optional.empty());
        when(productoRepository.findByEliminadoFalse()).thenReturn(Collections.emptyList());

        assertThrows(IllegalArgumentException.class, () ->
                ordenCompraService.agregarProducto(clienteTest, "prod-inexistente", 1));
    }

    @Test
    @DisplayName("agregarProducto con cliente nulo lanza IllegalArgumentException")
    void agregarProducto_clienteNulo_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                ordenCompraService.agregarProducto(null, "prod-uuid-1", 1));
    }

    // ==================== ACTUALIZAR CANTIDAD ====================

    @Test
    @DisplayName("actualizarCantidad actualiza cantidad del ítem y recalcula total")
    void actualizarCantidad_detalleExistente_actualizaCantidadYRecalculaTotal() {
        DetalleCompra item = DetalleCompra.builder()
                .id("det-1")
                .ordenCompra(carritoTest)
                .producto(productoTest)
                .cantidad(2)
                .precioUnitario(150.0)
                .subtotal(300.0)
                .eliminado(false)
                .build();
        carritoTest.getDetalles().add(item);
        carritoTest.setTotal(300.0);

        when(ordenCompraRepository.findByClienteAndEstadoOrdenCompraAndEliminadoFalse(
                clienteTest, EstadoOrdenCompra.PENDIENTE_COMPLETAR))
                .thenReturn(Optional.of(carritoTest));
        when(detalleCompraRepository.save(any(DetalleCompra.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ordenCompraRepository.save(any(OrdenCompra.class))).thenAnswer(inv -> inv.getArgument(0));

        OrdenCompra resultado = ordenCompraService.actualizarCantidad(clienteTest, "det-1", 5);

        assertNotNull(resultado);
        assertEquals(5, item.getCantidad());
        assertEquals(750.0, item.getSubtotal());
        assertEquals(750.0, resultado.getTotal());
    }

    @Test
    @DisplayName("actualizarCantidad con cantidad <= 0 lanza IllegalArgumentException")
    void actualizarCantidad_cantidadInvalida_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                ordenCompraService.actualizarCantidad(clienteTest, "det-1", 0));
        assertThrows(IllegalArgumentException.class, () ->
                ordenCompraService.actualizarCantidad(clienteTest, "det-1", -2));
    }

    @Test
    @DisplayName("actualizarCantidad con ítem inexistente lanza IllegalArgumentException")
    void actualizarCantidad_detalleNoPerteneceAlCarrito_lanzaIllegalArgumentException() {
        when(ordenCompraRepository.findByClienteAndEstadoOrdenCompraAndEliminadoFalse(
                clienteTest, EstadoOrdenCompra.PENDIENTE_COMPLETAR))
                .thenReturn(Optional.of(carritoTest));

        assertThrows(IllegalArgumentException.class, () ->
                ordenCompraService.actualizarCantidad(clienteTest, "det-fantasma", 3));
    }

    // ==================== ELIMINAR PRODUCTO ====================

    @Test
    @DisplayName("eliminarProducto marca el detalle como eliminado y recalcula total")
    void eliminarProducto_detalleExistente_marcaEliminadoYRecalculaTotal() {
        DetalleCompra item1 = DetalleCompra.builder()
                .id("det-1")
                .ordenCompra(carritoTest)
                .producto(productoTest)
                .cantidad(1)
                .precioUnitario(150.0)
                .subtotal(150.0)
                .eliminado(false)
                .build();
        DetalleCompra item2 = DetalleCompra.builder()
                .id("det-2")
                .ordenCompra(carritoTest)
                .producto(productoTest)
                .cantidad(2)
                .precioUnitario(50.0)
                .subtotal(100.0)
                .eliminado(false)
                .build();
        carritoTest.getDetalles().add(item1);
        carritoTest.getDetalles().add(item2);

        when(ordenCompraRepository.findByClienteAndEstadoOrdenCompraAndEliminadoFalse(
                clienteTest, EstadoOrdenCompra.PENDIENTE_COMPLETAR))
                .thenReturn(Optional.of(carritoTest));
        when(detalleCompraRepository.save(any(DetalleCompra.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ordenCompraRepository.save(any(OrdenCompra.class))).thenAnswer(inv -> inv.getArgument(0));

        OrdenCompra resultado = ordenCompraService.eliminarProducto(clienteTest, "det-1");

        assertNotNull(resultado);
        assertTrue(item1.isEliminado());
        assertFalse(item2.isEliminado());
        assertEquals(100.0, resultado.getTotal());
    }

    // ==================== VACIAR CARRITO ====================

    @Test
    @DisplayName("vaciarCarrito da de baja lógica a todos los ítems y total queda en 0.0")
    void vaciarCarrito_marcaTodosEliminados() {
        DetalleCompra item1 = DetalleCompra.builder()
                .id("det-1")
                .ordenCompra(carritoTest)
                .cantidad(1)
                .precioUnitario(100.0)
                .subtotal(100.0)
                .eliminado(false)
                .build();
        carritoTest.getDetalles().add(item1);

        when(ordenCompraRepository.findByClienteAndEstadoOrdenCompraAndEliminadoFalse(
                clienteTest, EstadoOrdenCompra.PENDIENTE_COMPLETAR))
                .thenReturn(Optional.of(carritoTest));
        when(detalleCompraRepository.save(any(DetalleCompra.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ordenCompraRepository.save(any(OrdenCompra.class))).thenAnswer(inv -> inv.getArgument(0));

        OrdenCompra resultado = ordenCompraService.vaciarCarrito(clienteTest);

        assertNotNull(resultado);
        assertTrue(item1.isEliminado());
        assertEquals(0.0, resultado.getTotal());
        assertEquals(0, ordenCompraService.obtenerItemsActivos(resultado).size());
    }

    // ==================== OBTENER O ASOCIAR CLIENTE ====================

    @Test
    @DisplayName("obtenerOAsociarCliente retorna el cliente existente asociado a la persona del usuario")
    void obtenerOAsociarCliente_conPersonaCliente_retornaCliente() {
        Usuario usuario = Usuario.builder()
                .id("user-1")
                .nombreUsuario("augusto@zero.com")
                .persona(clienteTest)
                .rol(RolUsuario.CLIENTE)
                .build();

        when(usuarioRepository.findById("user-1")).thenReturn(Optional.of(usuario));

        Cliente resultado = ordenCompraService.obtenerOAsociarCliente(usuario);

        assertNotNull(resultado);
        assertEquals(clienteTest.getNumeroDocumento(), resultado.getNumeroDocumento());
    }

    @Test
    @DisplayName("obtenerOAsociarCliente con usuario cliente sin persona previa crea nuevo cliente")
    void obtenerOAsociarCliente_usuarioSinPersona_creaYAsociaCliente() {
        Usuario usuario = Usuario.builder()
                .id("user-sin-persona")
                .nombreUsuario("cliente@zero.com")
                .rol(RolUsuario.CLIENTE)
                .build();

        Nacionalidad nac = Nacionalidad.builder().id("nac-01").nombre("Argentina").build();
        when(usuarioRepository.findById("user-sin-persona")).thenReturn(Optional.of(usuario));
        when(clienteRepository.findByNumeroDocumentoAndEliminadoFalse(anyString())).thenReturn(Optional.empty());
        when(nacionalidadRepository.findByEliminadoFalse()).thenReturn(List.of(nac));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Cliente resultado = ordenCompraService.obtenerOAsociarCliente(usuario);

        assertNotNull(resultado);
        assertEquals(resultado, usuario.getPersona());
        verify(clienteRepository, times(1)).save(any(Cliente.class));
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    @DisplayName("obtenerOAsociarCliente con usuario administrador lanza IllegalArgumentException")
    void obtenerOAsociarCliente_usuarioAdmin_lanzaIllegalArgumentException() {
        Usuario admin = Usuario.builder()
                .id("admin-1")
                .nombreUsuario("admin@zero.com")
                .rol(RolUsuario.ADMINISTRATIVO)
                .build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ordenCompraService.obtenerOAsociarCliente(admin)
        );

        assertTrue(ex.getMessage().contains("CLIENTE"));
        verifyNoInteractions(clienteRepository);
    }

    @Test
    @DisplayName("obtenerOAsociarCliente con usuario jefe lanza IllegalArgumentException")
    void obtenerOAsociarCliente_usuarioJefe_lanzaIllegalArgumentException() {
        Usuario jefe = Usuario.builder()
                .id("jefe-1")
                .nombreUsuario("jefe@zero.com")
                .rol(RolUsuario.JEFE)
                .build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ordenCompraService.obtenerOAsociarCliente(jefe)
        );

        assertTrue(ex.getMessage().contains("CLIENTE"));
        verifyNoInteractions(clienteRepository);
    }

    @Test
    @DisplayName("obtenerOAsociarCliente con usuario nulo lanza IllegalArgumentException")
    void obtenerOAsociarCliente_usuarioNulo_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> ordenCompraService.obtenerOAsociarCliente(null));
    }
}
