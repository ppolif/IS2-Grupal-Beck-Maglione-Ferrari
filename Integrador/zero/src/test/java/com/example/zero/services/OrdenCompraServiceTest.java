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
                .id("cli-uuid-1")
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
    @DisplayName("agregarProducto agrega un nuevo detalle al carrito y recalcula totales")
    void agregarProducto_nuevoItem_agregaYRecalcula() {
        when(ordenCompraRepository.findByClienteAndEstadoOrdenCompraAndEliminadoFalse(
                clienteTest, EstadoOrdenCompra.PENDIENTE_COMPLETAR))
                .thenReturn(Optional.of(carritoTest));
        when(productoRepository.findActive("prod-uuid-1")).thenReturn(Optional.of(productoTest));
        when(productoService.obtenerPrecioActual("prod-uuid-1")).thenReturn(150.0);
        when(detalleCompraRepository.save(any(DetalleCompra.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ordenCompraRepository.save(any(OrdenCompra.class))).thenAnswer(inv -> inv.getArgument(0));

        OrdenCompra resultado = ordenCompraService.agregarProducto(clienteTest, "prod-uuid-1", 2);

        assertNotNull(resultado);
        assertEquals(300.0, resultado.getTotal());
        assertEquals(1, resultado.getDetalles().size());
        assertEquals(2, resultado.getDetalles().get(0).getCantidad());
        assertEquals(300.0, resultado.getDetalles().get(0).getSubtotal());
        verify(detalleCompraRepository, times(1)).save(any(DetalleCompra.class));
    }

    @Test
    @DisplayName("agregarProducto incrementa cantidad si el producto ya existía en el carrito")
    void agregarProducto_productoYaExistente_incrementaCantidad() {
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

        when(ordenCompraRepository.findByClienteAndEstadoOrdenCompraAndEliminadoFalse(
                clienteTest, EstadoOrdenCompra.PENDIENTE_COMPLETAR))
                .thenReturn(Optional.of(carritoTest));
        when(productoRepository.findActive("prod-uuid-1")).thenReturn(Optional.of(productoTest));
        when(productoService.obtenerPrecioActual("prod-uuid-1")).thenReturn(150.0);
        when(detalleCompraRepository.save(any(DetalleCompra.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ordenCompraRepository.save(any(OrdenCompra.class))).thenAnswer(inv -> inv.getArgument(0));

        OrdenCompra resultado = ordenCompraService.agregarProducto(clienteTest, "prod-uuid-1", 2);

        assertNotNull(resultado);
        assertEquals(450.0, resultado.getTotal());
        assertEquals(1, resultado.getDetalles().size());
        assertEquals(3, itemExistente.getCantidad());
        assertEquals(450.0, itemExistente.getSubtotal());
    }

    @Test
    @DisplayName("agregarProducto con cantidad menor o igual a cero lanza IllegalArgumentException")
    void agregarProducto_cantidadInvalida_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () ->
                ordenCompraService.agregarProducto(clienteTest, "prod-uuid-1", 0));
        assertThrows(IllegalArgumentException.class, () ->
                ordenCompraService.agregarProducto(clienteTest, "prod-uuid-1", -1));
    }

    @Test
    @DisplayName("actualizarCantidad modifica la cantidad de un ítem existente y recalcula total")
    void actualizarCantidad_itemExistente_actualizaYRecalcula() {
        DetalleCompra item = DetalleCompra.builder()
                .id("det-1")
                .ordenCompra(carritoTest)
                .producto(productoTest)
                .cantidad(1)
                .precioUnitario(100.0)
                .subtotal(100.0)
                .eliminado(false)
                .build();
        carritoTest.getDetalles().add(item);

        when(ordenCompraRepository.findByClienteAndEstadoOrdenCompraAndEliminadoFalse(
                clienteTest, EstadoOrdenCompra.PENDIENTE_COMPLETAR))
                .thenReturn(Optional.of(carritoTest));
        when(detalleCompraRepository.save(any(DetalleCompra.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ordenCompraRepository.save(any(OrdenCompra.class))).thenAnswer(inv -> inv.getArgument(0));

        OrdenCompra resultado = ordenCompraService.actualizarCantidad(clienteTest, "det-1", 4);

        assertNotNull(resultado);
        assertEquals(400.0, resultado.getTotal());
        assertEquals(4, item.getCantidad());
        assertEquals(400.0, item.getSubtotal());
    }

    @Test
    @DisplayName("eliminarProducto realiza baja lógica del detalle y recalcula total")
    void eliminarProducto_itemExistente_bajaLogicaYRecalcula() {
        DetalleCompra item1 = DetalleCompra.builder()
                .id("det-1")
                .ordenCompra(carritoTest)
                .producto(productoTest)
                .cantidad(1)
                .precioUnitario(100.0)
                .subtotal(100.0)
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
        assertEquals(clienteTest.getId(), resultado.getId());
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
}

