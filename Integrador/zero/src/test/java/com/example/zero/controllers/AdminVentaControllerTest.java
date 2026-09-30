package com.example.zero.controllers;

import com.example.zero.entidades.compra.Detalle;
import com.example.zero.entidades.compra.Factura;
import com.example.zero.entidades.compra.FormaDePago;
import com.example.zero.entidades.compraCliente.FacturaCliente;
import com.example.zero.entidades.compraCliente.OrdenCompra;
import com.example.zero.entidades.compraProveedor.FacturaProveedor;
import com.example.zero.entidades.persona.Cliente;
import com.example.zero.entidades.persona.Usuario;
import com.example.zero.entidades.producto.Categoria;
import com.example.zero.entidades.producto.Producto;
import com.example.zero.entidades.producto.SubCategoria;
import com.example.zero.enums.TipoDePago;
import com.example.zero.services.producto.ProductoService;
import com.example.zero.services.VentaService;
import com.example.zero.services.persona.ClienteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminVentaControllerTest {

    @Mock
    private VentaService ventaService;

    @Mock
    private ProductoService productoService;

    @Mock
    private ClienteService clienteService;

    @Mock
    private com.example.zero.services.StockService stockService;

    @Mock
    private Model model;

    @InjectMocks
    private AdminVentaController controller;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
    }

    @Test
    void showRegistrarVentaForm_agregaProductosClientesYRetornaVista() {
        Producto p1 = Producto.builder().id("p1").nombre("Zapatillas").build();
        when(productoService.listarActivos()).thenReturn(List.of(p1));
        when(productoService.obtenerPrecioActual("p1")).thenReturn(3500.0);
        when(stockService.calcularStockActual("p1")).thenReturn(25);
        when(clienteService.listarActivos()).thenReturn(Collections.emptyList());

        String vista = controller.showRegistrarVentaForm(model, null);

        assertEquals("admin/registrar-venta", vista);
        verify(model).addAttribute(eq("productos"), anyList());
        verify(model).addAttribute(eq("clientes"), anyList());
        verify(model).addAttribute(eq("formasDePago"), any());
        verify(model, never()).addAttribute(eq("errorMessage"), anyString());
    }

    @Test
    void showRegistrarVentaForm_conParametroError_agregaErrorMessage() {
        when(productoService.listarActivos()).thenReturn(Collections.emptyList());
        when(clienteService.listarActivos()).thenReturn(Collections.emptyList());

        String vista = controller.showRegistrarVentaForm(model, "Error de validación previo");

        assertEquals("admin/registrar-venta", vista);
        verify(model).addAttribute("errorMessage", "Error de validación previo");
    }

    @Test
    void procesarVenta_conExito_redireccionaAOrdersConSuccessCreated() {
        Factura factura = Factura.builder().id("fac-1").numeroFactura(1001L).build();
        when(ventaService.registrarVenta(
                eq("12345678"), eq("Juan"), eq("Perez"), eq("juan@test.com"), eq("EFECTIVO"),
                eq(List.of("p1")), eq(List.of(2))
        )).thenReturn(factura);

        String vista = controller.procesarVenta(
                "12345678", "Juan", "Perez", "juan@test.com", "EFECTIVO",
                List.of("p1"), List.of(2), model
        );

        assertEquals("redirect:/admin/orders?success=created", vista);
        verify(ventaService).registrarVenta("12345678", "Juan", "Perez", "juan@test.com", "EFECTIVO", List.of("p1"), List.of(2));
    }

    @Test
    void procesarVenta_conErrorDeValidacion_recargaFormularioConDatosYMensaje() {
        when(ventaService.registrarVenta(any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("El DNI del cliente no puede estar vacío"));
        when(productoService.listarActivos()).thenReturn(Collections.emptyList());
        when(clienteService.listarActivos()).thenReturn(Collections.emptyList());

        String vista = controller.procesarVenta(
                "", "Juan", "Perez", "juan@test.com", "EFECTIVO",
                List.of("p1"), List.of(2), model
        );

        assertEquals("admin/registrar-venta", vista);
        verify(model).addAttribute("errorMessage", "El DNI del cliente no puede estar vacío");
        verify(model).addAttribute("clienteDni", "");
        verify(model).addAttribute("clienteNombre", "Juan");
        verify(model).addAttribute(eq("productos"), anyList());
    }

    @Test
    void procesarVenta_conStockInsuficiente_recargaFormularioYRenderizaErrorYRollback() {
        when(ventaService.registrarVenta(any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Stock insuficiente para: Zapatillas"));

        Producto p = Producto.builder().id("p1").nombre("Zapatillas").build();
        when(productoService.listarActivos()).thenReturn(List.of(p));
        when(productoService.obtenerPrecioActual("p1")).thenReturn(3500.0);
        when(stockService.calcularStockActual("p1")).thenReturn(0);
        when(clienteService.listarActivos()).thenReturn(Collections.emptyList());

        String vista = controller.procesarVenta(
                "12345678", "Juan", "Perez", "juan@test.com", "EFECTIVO",
                List.of("p1"), List.of(2), model
        );

        assertEquals("admin/registrar-venta", vista);
        verify(model).addAttribute("errorMessage", "Stock insuficiente para: Zapatillas");
        verify(model).addAttribute(eq("productos"), anyList());
    }

    @Test
    void listOrders_sinKeyword_retornaTodasLasFacturasEnVista() {
        Factura f1 = Factura.builder()
                .id("f1")
                .numeroFactura(1001L)
                .fechaFactura(LocalDateTime.now())
                .totalPagado(5000.0)
                .build();
        when(ventaService.listarVentas()).thenReturn(List.of(f1));

        String vista = controller.listOrders(model, null, null, null);

        assertEquals("admin/tables-basic", vista);
        verify(model).addAttribute(eq("orders"), anyList());
        verify(model).addAttribute("keyword", null);
    }

    @Test
    void listOrders_conKeyword_filtraPorNombreOComprobante() {
        Cliente c1 = Cliente.builder().nombre("Carlos").apellido("Tevez").build();
        OrdenCompra o1 = OrdenCompra.builder().cliente(c1).build();
        Factura f1 = FacturaCliente.builder().id("f1").numeroFactura(1001L).ordenCompra(o1).build();

        Cliente c2 = Cliente.builder().nombre("Lionel").apellido("Messi").build();
        OrdenCompra o2 = OrdenCompra.builder().cliente(c2).build();
        Factura f2 = FacturaCliente.builder().id("f2").numeroFactura(1002L).ordenCompra(o2).build();

        when(ventaService.listarVentas()).thenReturn(List.of(f1, f2));
        when(ventaService.obtenerNombreComprobante(f1)).thenReturn("Carlos Tevez");
        when(ventaService.obtenerNombreComprobante(f2)).thenReturn("Lionel Messi");

        String vista = controller.listOrders(model, "Lionel", null, null);

        assertEquals("admin/tables-basic", vista);
        verify(model).addAttribute(eq("orders"), argThat(list -> ((List<?>) list).size() == 1));
    }

    @Test
    void listOrders_conSuccessCreatedYDeleted_agregaMensajesExito() {
        when(ventaService.listarVentas()).thenReturn(Collections.emptyList());

        controller.listOrders(model, null, "created", null);
        verify(model).addAttribute("successMessage", "Venta registrada exitosamente con su orden de compra.");

        controller.listOrders(model, null, "deleted", null);
        verify(model).addAttribute("successMessage", "Orden de venta eliminada exitosamente.");
    }

    @Test
    void listOrders_conError_agregaErrorMessage() {
        when(ventaService.listarVentas()).thenReturn(Collections.emptyList());

        controller.listOrders(model, null, null, "Error al eliminar");
        verify(model).addAttribute("errorMessage", "Error al eliminar");
    }

    @Test
    void deleteOrder_conExito_redireccionaConSuccessDeleted() {
        String vista = controller.deleteOrder("f1");

        assertEquals("redirect:/admin/orders?success=deleted", vista);
        verify(ventaService).eliminarVenta("f1");
    }

    @Test
    void deleteOrder_conError_redireccionaConParametroError() {
        doThrow(new RuntimeException("No existe orden")).when(ventaService).eliminarVenta("f-invalida");

        String vista = controller.deleteOrder("f-invalida");

        assertEquals("redirect:/admin/orders?error=No existe orden", vista);
    }

    @Test
    void orderDetail_conExito_retornaVistaConFacturaYOrder() {
        Factura factura = Factura.builder().id("f1").numeroFactura(1001L).build();
        when(ventaService.buscarFacturaPorIdentificador("f1")).thenReturn(factura);

        org.springframework.web.servlet.mvc.support.RedirectAttributes ra = mock(org.springframework.web.servlet.mvc.support.RedirectAttributes.class);
        String vista = controller.orderDetail("f1", model, ra);

        assertEquals("admin/order-detail", vista);
        verify(model).addAttribute("factura", factura);
        verify(model).addAttribute("order", factura);
    }

    @Test
    void orderDetail_noExistente_redireccionaAOrdersConError() {
        when(ventaService.buscarFacturaPorIdentificador("f-999")).thenReturn(null);

        org.springframework.web.servlet.mvc.support.RedirectAttributes ra = mock(org.springframework.web.servlet.mvc.support.RedirectAttributes.class);
        String vista = controller.orderDetail("f-999", model, ra);

        assertEquals("redirect:/admin/orders", vista);
        verify(ra).addFlashAttribute(eq("errorMessage"), anyString());
    }

    @Test
    void factura_helpersRetornanValoresCorrectos() {
        Categoria cat = Categoria.builder().id("c1").nombre("Deportes").build();
        SubCategoria subCat = SubCategoria.builder().id("sc1").nombre("Calzado").categoria(cat).build();
        Producto prod = Producto.builder().id("p1").nombre("Zapatillas Nike").subCategoria(subCat).build();

        Detalle detalle = Detalle.builder()
                .id("d1")
                .producto(prod)
                .cantidad(2)
                .subtotal(5000.0)
                .build();

        Usuario user = Usuario.builder().nombreUsuario("carlos@example.com").foto("https://ejemplo.com/avatar.jpg").build();
        Cliente cliente = Cliente.builder()
                .numeroDocumento("12345678")
                .nombre("Carlos")
                .apellido("Perez")
                .usuario(user)
                .build();

        FormaDePago forma = FormaDePago.builder()
                .tipoPago(TipoDePago.TARJETA_CREDITO)
                .build();

        OrdenCompra orden = OrdenCompra.builder().cliente(cliente).build();

        FacturaCliente factura = FacturaCliente.builder()
                .id("fac-123")
                .numeroFactura(1055L)
                .fechaFactura(LocalDateTime.of(2026, 9, 21, 10, 0))
                .totalPagado(5000.0)
                .ordenCompra(orden)
                .formaDePago(forma)
                .detalles(new HashSet<>(List.of(detalle)))
                .build();

        assertEquals(1055L, factura.getNumeroFactura());
        assertEquals("Carlos", factura.getOrdenCompra().getCliente().getNombre());
        assertEquals(TipoDePago.TARJETA_CREDITO, factura.getFormaDePago().getTipoPago());
        assertEquals(5000.0, factura.getTotalPagado());
        assertEquals(1, factura.getDetalles().size());
        assertEquals("Zapatillas Nike", detalle.getProducto().getNombre());
        assertEquals(2, detalle.getCantidad());
        assertEquals(2500.0, Math.round((detalle.getSubtotal() / detalle.getCantidad()) * 100.0) / 100.0);
    }

    @Test
    void orderDetail_paraFacturaProveedor_agregaProveedorYSucursal() {
        com.example.zero.entidades.compraProveedor.Proveedor prov = com.example.zero.entidades.compraProveedor.Proveedor.builder()
                .id("prov-1")
                .razonSocial("Textil Central S.A.")
                .cuit("30-11223344-5")
                .build();

        com.example.zero.entidades.compraProveedor.FacturaProveedor fp = new com.example.zero.entidades.compraProveedor.FacturaProveedor();
        fp.setId("fp-1");
        fp.setNumeroFactura(5001L);
        fp.setProveedor(prov);
        fp.setTotalPagado(15000.0);

        Detalle detalle = Detalle.builder()
                .id("d-prov")
                .factura(fp)
                .cantidad(10)
                .subtotal(15000.0)
                .build();
        fp.setDetalles(new HashSet<>(List.of(detalle)));

        when(ventaService.buscarFacturaPorIdentificador("fp-1")).thenReturn(fp);

        org.springframework.web.servlet.mvc.support.RedirectAttributes ra = mock(org.springframework.web.servlet.mvc.support.RedirectAttributes.class);
        String vista = controller.orderDetail("fp-1", model, ra);

        assertEquals("admin/order-detail", vista);
        verify(model).addAttribute("factura", fp);
        verify(model).addAttribute("order", fp);
        verify(model).addAttribute("esCompraProveedor", true);
        verify(model).addAttribute("facturaProveedor", fp);
        verify(model).addAttribute("proveedor", prov);
        verify(model).addAttribute(eq("sucursal"), any());

        assertTrue(fp instanceof FacturaProveedor);
        assertEquals("Textil Central S.A.", fp.getProveedor().getRazonSocial());
        assertEquals("30-11223344-5", fp.getProveedor().getCuit());
    }
}

