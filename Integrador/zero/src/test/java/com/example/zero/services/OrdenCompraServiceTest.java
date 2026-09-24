package com.example.zero.services;

import com.example.zero.entidades.compraCliente.DetalleCompra;
import com.example.zero.entidades.compraCliente.OrdenCompra;
import com.example.zero.entidades.persona.Cliente;
import com.example.zero.entidades.persona.Nacionalidad;
import com.example.zero.entidades.persona.Usuario;
import com.example.zero.entidades.producto.Producto;
import com.example.zero.enums.EstadoOrdenCompra;
<<<<<<< HEAD
import com.example.zero.enums.TipoDocumento;
import com.example.zero.repositories.*;
import org.junit.jupiter.api.BeforeEach;
=======
import com.example.zero.enums.RolUsuario;
import com.example.zero.enums.TipoDocumento;
import com.example.zero.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
>>>>>>> augusto
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
<<<<<<< HEAD
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
=======
import java.util.*;
>>>>>>> augusto

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
<<<<<<< HEAD
        Nacionalidad nac = Nacionalidad.builder().id("nac-1").nombre("Argentina").build();
        clienteTest = Cliente.builder()
                .numeroDocumento("35123456")
                .nombre("Juan")
                .apellido("Pérez")
                .fechaNacimiento(LocalDate.of(1990, 5, 20))
=======
        Nacionalidad nac = Nacionalidad.builder().id("nac-01").nombre("Argentina").build();
        clienteTest = Cliente.builder()
                .id("cli-uuid-1")
                .numeroDocumento("12345678")
                .nombre("Juan")
                .apellido("Perez")
                .fechaNacimiento(LocalDate.of(1995, 5, 10))
>>>>>>> augusto
                .tipoDocumento(TipoDocumento.DNI)
                .nacionalidad(nac)
                .eliminado(false)
                .build();

        productoTest = Producto.builder()
<<<<<<< HEAD
                .id("prod-1")
                .codigo("ZAP-001")
                .nombre("Zapatilla Running")
                .talle("42")
=======
                .id("prod-uuid-1")
                .codigo("PROD-001")
                .nombre("Zapatilla Running")
                .precioActual(150.0)
>>>>>>> augusto
                .eliminado(false)
                .build();

        carritoTest = OrdenCompra.builder()
<<<<<<< HEAD
                .id("ord-1")
                .identificadorCompra("CART-12345678")
                .estadoOrdenCompra(EstadoOrdenCompra.PENDIENTE_COMPLETAR)
                .cliente(clienteTest)
=======
                .id("cart-uuid-1")
                .identificadorCompra("CART-12345678")
                .cliente(clienteTest)
                .estadoOrdenCompra(EstadoOrdenCompra.PENDIENTE_COMPLETAR)
>>>>>>> augusto
                .total(0.0)
                .eliminado(false)
                .detalles(new ArrayList<>())
                .build();
    }

<<<<<<< HEAD
    // ==================== METODOS DE CALCULO Y FILTRADO EN SERVICIO ====================

    @Test
    void recalcularTotal_calculaCorrectamenteSumaDetallesActivos() {
        DetalleCompra d1 = DetalleCompra.builder().cantidad(2).precioUnitario(100.0).subtotal(200.0).eliminado(false).build();
        DetalleCompra d2 = DetalleCompra.builder().cantidad(1).precioUnitario(350.5).subtotal(350.5).eliminado(false).build();
        DetalleCompra dEliminado = DetalleCompra.builder().cantidad(1).precioUnitario(500.0).subtotal(500.0).eliminado(true).build();

        OrdenCompra orden = OrdenCompra.builder().detalles(new ArrayList<>(List.of(d1, d2, dEliminado))).build();

        ordenCompraService.recalcularTotal(orden);

        assertEquals(550.5, orden.getTotal());
    }

    @Test
    void recalcularSubtotal_calculaMultiplicacionCantidadYPrecio() {
        DetalleCompra detalle = DetalleCompra.builder().cantidad(3).precioUnitario(199.99).build();

        ordenCompraService.recalcularSubtotal(detalle);

        assertEquals(599.97, detalle.getSubtotal());
    }

    @Test
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
    void contarItems_conOrdenCompra_cuentaDetallesActivos() {
        DetalleCompra d1 = DetalleCompra.builder().cantidad(3).eliminado(false).build();
        DetalleCompra d2 = DetalleCompra.builder().cantidad(2).eliminado(false).build();
        DetalleCompra dEliminado = DetalleCompra.builder().cantidad(5).eliminado(true).build();

        OrdenCompra orden = OrdenCompra.builder().detalles(List.of(d1, d2, dEliminado)).build();

        int total = ordenCompraService.contarItems(orden);

        assertEquals(5, total);
    }

    // ==================== OBTENER O CREAR CARRITO ====================

    @Test
    void obtenerOCrearCarrito_conCarritoExistente_retornaCarritoExistente() {
=======
    @Test
    @DisplayName("obtenerOCrearCarrito retorna carrito existente si ya está en estado PENDIENTE_COMPLETAR")
    void obtenerOCrearCarrito_existente_retornaCarritoExistente() {
>>>>>>> augusto
        when(ordenCompraRepository.findByClienteAndEstadoOrdenCompraAndEliminadoFalse(
                clienteTest, EstadoOrdenCompra.PENDIENTE_COMPLETAR))
                .thenReturn(Optional.of(carritoTest));

        OrdenCompra resultado = ordenCompraService.obtenerOCrearCarrito(clienteTest);

        assertNotNull(resultado);
<<<<<<< HEAD
        assertEquals("ord-1", resultado.getId());
        assertEquals(EstadoOrdenCompra.PENDIENTE_COMPLETAR, resultado.getEstadoOrdenCompra());
        verify(ordenCompraRepository, never()).save(any(OrdenCompra.class));
    }

    @Test
    void obtenerOCrearCarrito_sinCarrito_creaNuevoCarritoPendienteCompletar() {
        when(ordenCompraRepository.findByClienteAndEstadoOrdenCompraAndEliminadoFalse(
                clienteTest, EstadoOrdenCompra.PENDIENTE_COMPLETAR))
                .thenReturn(Optional.empty());

        when(ordenCompraRepository.save(any(OrdenCompra.class))).thenAnswer(inv -> {
            OrdenCompra oc = inv.getArgument(0);
            oc.setId("ord-nueva");
            return oc;
        });
=======
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
>>>>>>> augusto

        OrdenCompra resultado = ordenCompraService.obtenerOCrearCarrito(clienteTest);

        assertNotNull(resultado);
<<<<<<< HEAD
        assertEquals("ord-nueva", resultado.getId());
        assertEquals(EstadoOrdenCompra.PENDIENTE_COMPLETAR, resultado.getEstadoOrdenCompra());
        assertEquals(clienteTest, resultado.getCliente());
        assertFalse(resultado.isEliminado());
=======
        assertEquals(EstadoOrdenCompra.PENDIENTE_COMPLETAR, resultado.getEstadoOrdenCompra());
        assertEquals(clienteTest, resultado.getCliente());
>>>>>>> augusto
        verify(ordenCompraRepository, times(1)).save(any(OrdenCompra.class));
    }

    @Test
<<<<<<< HEAD
    void obtenerOCrearCarrito_clienteNulo_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> ordenCompraService.obtenerOCrearCarrito(null));
    }

    // ==================== AGREGAR PRODUCTO ====================

    @Test
    void agregarProducto_productoNuevo_creaDetalleYRecalculaTotal() {
        when(productoRepository.findActive("prod-1")).thenReturn(Optional.of(productoTest));
        when(productoService.obtenerPrecioActual("prod-1")).thenReturn(2500.0);
        when(ordenCompraRepository.findByClienteAndEstadoOrdenCompraAndEliminadoFalse(
                clienteTest, EstadoOrdenCompra.PENDIENTE_COMPLETAR))
                .thenReturn(Optional.of(carritoTest));

        when(detalleCompraRepository.save(any(DetalleCompra.class))).thenAnswer(inv -> {
            DetalleCompra dc = inv.getArgument(0);
            dc.setId("det-1");
            return dc;
        });
        when(ordenCompraRepository.save(any(OrdenCompra.class))).thenAnswer(inv -> inv.getArgument(0));

        OrdenCompra resultado = ordenCompraService.agregarProducto(clienteTest, "prod-1", 2);

        assertNotNull(resultado);
        assertEquals(5000.0, resultado.getTotal());
        assertEquals(1, resultado.getDetalles().size());
        assertEquals("det-1", resultado.getDetalles().get(0).getId());
        assertEquals(2, resultado.getDetalles().get(0).getCantidad());
        assertEquals(2500.0, resultado.getDetalles().get(0).getPrecioUnitario());
        assertEquals(5000.0, resultado.getDetalles().get(0).getSubtotal());

        verify(detalleCompraRepository, times(1)).save(any(DetalleCompra.class));
        verify(ordenCompraRepository, times(1)).save(carritoTest);
    }

    @Test
    void agregarProducto_productoExistenteEnCarrito_incrementaCantidadYRecalculaTotal() {
        DetalleCompra detalleExistente = DetalleCompra.builder()
                .id("det-1")
                .producto(productoTest)
                .ordenCompra(carritoTest)
                .cantidad(2)
                .precioUnitario(2000.0)
                .subtotal(4000.0)
                .eliminado(false)
                .build();
        carritoTest.getDetalles().add(detalleExistente);
        carritoTest.setTotal(4000.0);

        when(productoRepository.findActive("prod-1")).thenReturn(Optional.of(productoTest));
        when(productoService.obtenerPrecioActual("prod-1")).thenReturn(2000.0);
        when(ordenCompraRepository.findByClienteAndEstadoOrdenCompraAndEliminadoFalse(
                clienteTest, EstadoOrdenCompra.PENDIENTE_COMPLETAR))
                .thenReturn(Optional.of(carritoTest));
        when(detalleCompraRepository.save(any(DetalleCompra.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ordenCompraRepository.save(any(OrdenCompra.class))).thenAnswer(inv -> inv.getArgument(0));

        OrdenCompra resultado = ordenCompraService.agregarProducto(clienteTest, "prod-1", 3);

        assertEquals(5, detalleExistente.getCantidad());
        assertEquals(10000.0, detalleExistente.getSubtotal());
        assertEquals(10000.0, resultado.getTotal());
        verify(detalleCompraRepository, times(1)).save(detalleExistente);
    }

    @Test
    void agregarProducto_cantidadInvalida_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                ordenCompraService.agregarProducto(clienteTest, "prod-1", 0));
        assertThrows(IllegalArgumentException.class, () ->
                ordenCompraService.agregarProducto(clienteTest, "prod-1", -2));
    }

    @Test
    void agregarProducto_productoInexistente_lanzaIllegalArgumentException() {
        when(productoRepository.findActive("prod-inexistente")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                ordenCompraService.agregarProducto(clienteTest, "prod-inexistente", 1));
    }

    @Test
    void agregarProducto_clienteNulo_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                ordenCompraService.agregarProducto(null, "prod-1", 1));
    }

    // ==================== ACTUALIZAR CANTIDAD ====================

    @Test
    void actualizarCantidad_detalleExistente_actualizaCantidadYRecalculaTotal() {
        DetalleCompra detalle = DetalleCompra.builder()
                .id("det-1")
                .producto(productoTest)
                .ordenCompra(carritoTest)
                .cantidad(1)
                .precioUnitario(3000.0)
                .subtotal(3000.0)
                .eliminado(false)
                .build();
        carritoTest.getDetalles().add(detalle);
        carritoTest.setTotal(3000.0);
=======
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
>>>>>>> augusto

        when(ordenCompraRepository.findByClienteAndEstadoOrdenCompraAndEliminadoFalse(
                clienteTest, EstadoOrdenCompra.PENDIENTE_COMPLETAR))
                .thenReturn(Optional.of(carritoTest));
        when(detalleCompraRepository.save(any(DetalleCompra.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ordenCompraRepository.save(any(OrdenCompra.class))).thenAnswer(inv -> inv.getArgument(0));

        OrdenCompra resultado = ordenCompraService.actualizarCantidad(clienteTest, "det-1", 4);

<<<<<<< HEAD
        assertEquals(4, detalle.getCantidad());
        assertEquals(12000.0, detalle.getSubtotal());
        assertEquals(12000.0, resultado.getTotal());
    }

    @Test
    void actualizarCantidad_cantidadInvalida_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                ordenCompraService.actualizarCantidad(clienteTest, "det-1", 0));
        assertThrows(IllegalArgumentException.class, () ->
                ordenCompraService.actualizarCantidad(clienteTest, "det-1", -5));
    }

    @Test
    void actualizarCantidad_detalleNoPerteneceAlCarrito_lanzaIllegalArgumentException() {
        when(ordenCompraRepository.findByClienteAndEstadoOrdenCompraAndEliminadoFalse(
                clienteTest, EstadoOrdenCompra.PENDIENTE_COMPLETAR))
                .thenReturn(Optional.of(carritoTest));

        assertThrows(IllegalArgumentException.class, () ->
                ordenCompraService.actualizarCantidad(clienteTest, "det-inexistente", 3));
    }

    // ==================== ELIMINAR PRODUCTO ====================

    @Test
    void eliminarProducto_detalleExistente_marcaEliminadoYRecalculaTotal() {
        DetalleCompra detalle1 = DetalleCompra.builder()
                .id("det-1")
                .producto(productoTest)
                .ordenCompra(carritoTest)
                .cantidad(2)
                .precioUnitario(1000.0)
                .subtotal(2000.0)
                .eliminado(false)
                .build();
        DetalleCompra detalle2 = DetalleCompra.builder()
                .id("det-2")
                .producto(productoTest)
                .ordenCompra(carritoTest)
                .cantidad(1)
                .precioUnitario(500.0)
                .subtotal(500.0)
                .eliminado(false)
                .build();
        carritoTest.getDetalles().add(detalle1);
        carritoTest.getDetalles().add(detalle2);
        carritoTest.setTotal(2500.0);
=======
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
>>>>>>> augusto

        when(ordenCompraRepository.findByClienteAndEstadoOrdenCompraAndEliminadoFalse(
                clienteTest, EstadoOrdenCompra.PENDIENTE_COMPLETAR))
                .thenReturn(Optional.of(carritoTest));
        when(detalleCompraRepository.save(any(DetalleCompra.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ordenCompraRepository.save(any(OrdenCompra.class))).thenAnswer(inv -> inv.getArgument(0));

        OrdenCompra resultado = ordenCompraService.eliminarProducto(clienteTest, "det-1");

<<<<<<< HEAD
        assertTrue(detalle1.isEliminado());
        assertEquals(500.0, resultado.getTotal());
    }

    // ==================== VACIAR CARRITO ====================

    @Test
    void vaciarCarrito_conProductos_marcaTodosEliminadosYTotalCero() {
        DetalleCompra detalle1 = DetalleCompra.builder()
                .id("det-1")
                .cantidad(2)
                .subtotal(2000.0)
                .eliminado(false)
                .build();
        DetalleCompra detalle2 = DetalleCompra.builder()
                .id("det-2")
                .cantidad(1)
                .subtotal(1000.0)
                .eliminado(false)
                .build();
        carritoTest.getDetalles().add(detalle1);
        carritoTest.getDetalles().add(detalle2);
        carritoTest.setTotal(3000.0);
=======
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
>>>>>>> augusto

        when(ordenCompraRepository.findByClienteAndEstadoOrdenCompraAndEliminadoFalse(
                clienteTest, EstadoOrdenCompra.PENDIENTE_COMPLETAR))
                .thenReturn(Optional.of(carritoTest));
<<<<<<< HEAD
=======
        when(detalleCompraRepository.save(any(DetalleCompra.class))).thenAnswer(inv -> inv.getArgument(0));
>>>>>>> augusto
        when(ordenCompraRepository.save(any(OrdenCompra.class))).thenAnswer(inv -> inv.getArgument(0));

        OrdenCompra resultado = ordenCompraService.vaciarCarrito(clienteTest);

<<<<<<< HEAD
        assertTrue(detalle1.isEliminado());
        assertTrue(detalle2.isEliminado());
        assertEquals(0.0, resultado.getTotal());
    }

    @Test
    void contarItems_cuentaCorrectamenteUnidades() {
        DetalleCompra d1 = DetalleCompra.builder().cantidad(2).eliminado(false).build();
        DetalleCompra d2 = DetalleCompra.builder().cantidad(3).eliminado(false).build();
        DetalleCompra dEliminado = DetalleCompra.builder().cantidad(10).eliminado(true).build();
        carritoTest.getDetalles().addAll(List.of(d1, d2, dEliminado));

        when(ordenCompraRepository.findByClienteAndEstadoOrdenCompraAndEliminadoFalse(
                clienteTest, EstadoOrdenCompra.PENDIENTE_COMPLETAR))
                .thenReturn(Optional.of(carritoTest));

        int total = ordenCompraService.contarItems(clienteTest);
        assertEquals(5, total);
    }

    // ==================== OBTENER O ASOCIAR CLIENTE ====================

    @Test
    void obtenerOAsociarCliente_usuarioConPersonaCliente_retornaCliente() {
        Usuario usuario = Usuario.builder().id("usr-1").persona(clienteTest).build();

        Cliente resultado = ordenCompraService.obtenerOAsociarCliente(usuario);

        assertEquals(clienteTest, resultado);
    }

    @Test
    void obtenerOAsociarCliente_usuarioSinPersona_creaYAsociaCliente() {
        Usuario usuario = Usuario.builder().id("usr-sin-persona").nombreUsuario("cliente@zero.com").build();
        Nacionalidad nac = Nacionalidad.builder().id("nac-1").nombre("Argentina").build();

        when(nacionalidadRepository.findByEliminadoFalse()).thenReturn(List.of(nac));
        when(clienteRepository.findByNumeroDocumentoAndEliminadoFalse(anyString())).thenReturn(Optional.empty());
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));
=======
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
>>>>>>> augusto

        Cliente resultado = ordenCompraService.obtenerOAsociarCliente(usuario);

        assertNotNull(resultado);
<<<<<<< HEAD
        assertEquals(resultado, usuario.getPersona());
        verify(clienteRepository, times(1)).save(any(Cliente.class));
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    void obtenerOAsociarCliente_usuarioNulo_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> ordenCompraService.obtenerOAsociarCliente(null));
    }
}
=======
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

>>>>>>> augusto
