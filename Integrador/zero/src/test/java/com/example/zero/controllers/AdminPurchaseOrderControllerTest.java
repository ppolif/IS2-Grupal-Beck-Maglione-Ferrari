package com.example.zero.controllers;

import com.example.zero.entidades.compraProveedor.FacturaProveedor;
import com.example.zero.entidades.compraProveedor.Proveedor;
import com.example.zero.entidades.producto.Producto;
import com.example.zero.services.CompraProveedorService;
import com.example.zero.services.ProveedorService;
import com.example.zero.services.producto.ProductoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminPurchaseOrderControllerTest {

    @Mock
    private CompraProveedorService compraProveedorService;

    @Mock
    private ProveedorService proveedorService;

    @Mock
    private ProductoService productoService;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private AdminPurchaseOrderController controller;

    @Test
    void showRegistrarCompraForm_retornaVistaConAtributos() {
        when(proveedorService.listarActivos()).thenReturn(List.of(
                Proveedor.builder().id("pr1").razonSocial("Textil").cuit("30-11111111-1").build()
        ));
        when(productoService.listarActivos()).thenReturn(List.of(
                Producto.builder().id("p1").nombre("Zapatillas").build()
        ));

        String vista = controller.showRegistrarCompraForm(model, null, null);

        assertEquals("admin/registrar-compra", vista);
        verify(model).addAttribute(eq("proveedores"), anyList());
        verify(model).addAttribute(eq("productos"), anyList());
        verify(model).addAttribute(eq("formasDePago"), any());
        verify(model).addAttribute(eq("estadosFactura"), any());
    }

    @Test
    void registrarCompra_conDatosValidos_redirigeConExito() {
        when(compraProveedorService.registrarCompraProveedor(
                anyString(), any(), any(), anyString(), anyString(), anyList(), anyList(), anyList()
        )).thenReturn(new FacturaProveedor());

        String vista = controller.registrarCompra(
                "pr1", 1001L, null, "TRANSFERENCIA", "PAGADA",
                List.of("p1"), List.of(5), List.of(200.0),
                model, redirectAttributes
        );

        assertEquals("redirect:/admin/registrar-compra?success=created", vista);
        verify(redirectAttributes).addFlashAttribute(eq("successMessage"), anyString());
    }

    @Test
    void registrarCompra_conError_recargaVista() {
        when(compraProveedorService.registrarCompraProveedor(
                anyString(), any(), any(), anyString(), anyString(), anyList(), anyList(), anyList()
        )).thenThrow(new IllegalArgumentException("Debe seleccionar al menos un producto"));

        String vista = controller.registrarCompra(
                "pr1", 1001L, null, "TRANSFERENCIA", "PAGADA",
                List.of("p1"), List.of(5), List.of(200.0),
                model, redirectAttributes
        );

        assertEquals("admin/registrar-compra", vista);
        verify(model).addAttribute(eq("errorMessage"), eq("Debe seleccionar al menos un producto"));
    }

    @Test
    void eliminarCompra_redirigeConExito() {
        doNothing().when(compraProveedorService).eliminarCompraProveedor("fp-1");

        String vista = controller.eliminarCompra("fp-1", redirectAttributes);

        assertEquals("redirect:/admin/registrar-compra?success=deleted", vista);
        verify(compraProveedorService).eliminarCompraProveedor("fp-1");
    }
}
