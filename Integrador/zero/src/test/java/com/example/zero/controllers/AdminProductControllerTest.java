package com.example.zero.controllers;

import com.example.zero.entidades.producto.Producto;
import com.example.zero.entidades.producto.SubCategoria;
import com.example.zero.services.producto.ProductoService;
import com.example.zero.services.SubCategoriaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminProductControllerTest {

    @Mock
    private ProductoService productoService;

    @Mock
    private SubCategoriaService subCategoriaService;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private AdminProductController controller;

    @Test
    void listProducts_retornaVistaConProductosYPrecios() {
        Producto prod = Producto.builder().id("p1").nombre("Zapatillas").build();
        when(productoService.listarActivos()).thenReturn(List.of(prod));
        when(productoService.obtenerPrecioActual("p1")).thenReturn(100.0);

        String vista = controller.listProducts(model, null, null);

        assertEquals("admin/products", vista);
        verify(model).addAttribute(eq("products"), anyList());
        assertEquals(100.0, prod.getPrecioActual());
    }

    @Test
    void listProducts_conParametrosDeExito_agregaMensajeCorrespondiente() {
        when(productoService.listarActivos()).thenReturn(Collections.emptyList());

        controller.listProducts(model, "created", null);
        verify(model).addAttribute("successMessage", "Producto creado exitosamente.");

        controller.listProducts(model, "updated", null);
        verify(model).addAttribute("successMessage", "Producto modificado exitosamente.");

        controller.listProducts(model, "deleted", null);
        verify(model).addAttribute("successMessage", "Producto dado de baja exitosamente.");
    }

    @Test
    void showCreateForm_retornaVistaConSubcategorias() {
        SubCategoria sub = SubCategoria.builder().id("sub1").nombre("Running").build();
        when(subCategoriaService.listarActivas()).thenReturn(List.of(sub));

        String vista = controller.showCreateForm(model);

        assertEquals("admin/product-form", vista);
        verify(model).addAttribute("subcategorias", List.of(sub));
        verify(model).addAttribute("isEdit", false);
    }

    @Test
    void createProduct_exitoso_redirigeAListado() {
        Producto creado = Producto.builder().id("p1").codigo("PROD-01").build();
        when(productoService.crearProducto("PROD-01", "Runner", "Desc", "42", "sub1", 120.0, true))
                .thenReturn(creado);

        String vista = controller.createProduct("PROD-01", "Runner", "Desc", "42", "sub1", 120.0, true, model, redirectAttributes);

        assertEquals("redirect:/admin/products?success=created", vista);
    }

    @Test
    void createProduct_conImagenValida_creaExitosamente() {
        MockMultipartFile file = new MockMultipartFile("imagen", "zap.jpg", "image/jpeg", new byte[]{1, 2});
        Producto creado = Producto.builder().id("p1").codigo("PROD-01").build();
        when(productoService.crearProducto("PROD-01", "Runner", "Desc", "42", "sub1", 120.0, true, file))
                .thenReturn(creado);

        String vista = controller.createProduct("PROD-01", "Runner", "Desc", "42", "sub1", 120.0, true, file, model, redirectAttributes);

        assertEquals("redirect:/admin/products?success=created", vista);
    }

    @Test
    void createProduct_sinImagen_retornaErrorFormulario() {
        when(subCategoriaService.listarActivas()).thenReturn(Collections.emptyList());

        String vista = controller.createProduct("PROD-01", "Runner", "Desc", "42", "sub1", 120.0, true, null, model, redirectAttributes);

        assertEquals("admin/product-form", vista);
        verify(model).addAttribute("errorMessage", "Debe seleccionar obligatoriamente una imagen del producto");
        verify(model).addAttribute("isEdit", false);
    }

    @Test
    void createProduct_errorValidacion_retornaFormularioConError() {
        when(productoService.crearProducto(any(), any(), any(), any(), any(), anyDouble(), anyBoolean()))
                .thenThrow(new IllegalArgumentException("El código ya existe"));
        when(subCategoriaService.listarActivas()).thenReturn(Collections.emptyList());

        String vista = controller.createProduct("DUPLICADO", "Runner", "Desc", "42", "sub1", 120.0, true, model, redirectAttributes);

        assertEquals("admin/product-form", vista);
        verify(model).addAttribute("errorMessage", "El código ya existe");
        verify(model).addAttribute("isEdit", false);
    }

    @Test
    void showEditForm_productoExistente_retornaVistaEdicion() {
        Producto prod = Producto.builder().id("p1").nombre("Remera").build();
        when(productoService.buscarPorId("p1")).thenReturn(prod);
        when(productoService.obtenerPrecioActual("p1")).thenReturn(50.0);
        when(subCategoriaService.listarActivas()).thenReturn(Collections.emptyList());

        String vista = controller.showEditForm("p1", model);

        assertEquals("admin/product-form", vista);
        verify(model).addAttribute("producto", prod);
        verify(model).addAttribute("precioActual", 50.0);
        verify(model).addAttribute("isEdit", true);
    }

    @Test
    void showEditForm_productoInexistente_redirigeConError() {
        when(productoService.buscarPorId("no-existe"))
                .thenThrow(new IllegalArgumentException("Producto no encontrado"));

        String vista = controller.showEditForm("no-existe", model);

        assertEquals("redirect:/admin/products?error=Producto no encontrado", vista);
    }

    @Test
    void updateProduct_exitoso_actualizaProductoYPrecio() {
        Producto prod = Producto.builder().id("p1").build();
        when(productoService.modificarProducto("p1", "Nuevo Nombre", "Nueva Desc", "L", "sub2", false))
                .thenReturn(prod);
        when(productoService.obtenerPrecioActual("p1")).thenReturn(50.0);

        String vista = controller.updateProduct("p1", "Nuevo Nombre", "Nueva Desc", "L", "sub2", 75.0, false, model);

        assertEquals("redirect:/admin/products?success=updated", vista);
        verify(productoService).actualizarPrecio("p1", 75.0);
    }

    @Test
    void updateProduct_error_retornaFormularioConError() {
        when(productoService.modificarProducto(any(), any(), any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Error al modificar"));
        when(subCategoriaService.listarActivas()).thenReturn(Collections.emptyList());

        String vista = controller.updateProduct("p1", "Nombre", "Desc", "L", "sub2", 75.0, false, model);

        assertEquals("admin/product-form", vista);
        verify(model).addAttribute("errorMessage", "Error al modificar");
        verify(model).addAttribute("isEdit", true);
    }

    @Test
    void deleteProductPost_eliminaYRedirige() {
        String vista = controller.deleteProductPost("p1");

        assertEquals("redirect:/admin/products?success=deleted", vista);
        verify(productoService).eliminarProducto("p1");
    }

    @Test
    void deleteProductGet_eliminaYRedirige() {
        String vista = controller.deleteProductGet("p1");

        assertEquals("redirect:/admin/products?success=deleted", vista);
        verify(productoService).eliminarProducto("p1");
    }
}
