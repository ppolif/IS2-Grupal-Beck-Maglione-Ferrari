package com.example.zero.controllers;

import com.example.zero.entidades.compraProveedor.Proveedor;
import com.example.zero.services.ProveedorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminProviderControllerTest {

    @Mock
    private ProveedorService proveedorService;

    @Mock
    private Model model;

    @InjectMocks
    private AdminProviderController controller;

    @Test
    void listProviders_retornaVistaYModelo() {
        Proveedor p = Proveedor.builder().id("p1").razonSocial("Textil").cuit("30-11111111-1").build();
        when(proveedorService.listarActivos()).thenReturn(List.of(p));

        String vista = controller.listProviders(model, null, null);

        assertEquals("admin/providers", vista);
        verify(model).addAttribute(eq("providers"), anyList());
    }

    @Test
    void listProviders_conParametrosDeExito_agregaMensajeCorrespondiente() {
        when(proveedorService.listarActivos()).thenReturn(Collections.emptyList());

        controller.listProviders(model, "created", null);
        verify(model).addAttribute("successMessage", "Proveedor registrado exitosamente.");

        controller.listProviders(model, "updated", null);
        verify(model).addAttribute("successMessage", "Proveedor modificado exitosamente.");

        controller.listProviders(model, "deleted", null);
        verify(model).addAttribute("successMessage", "Proveedor dado de baja exitosamente.");
    }

    @Test
    void showCreateForm_retornaVistaYModelo() {
        String vista = controller.showCreateForm(model);

        assertEquals("admin/provider-form", vista);
        verify(model).addAttribute(eq("proveedor"), any(Proveedor.class));
        verify(model).addAttribute("isEdit", false);
    }

    @Test
    void createProvider_exitoso_redirigeAListado() {
        Proveedor creado = Proveedor.builder().id("p1").razonSocial("Textil").cuit("30-11111111-1").build();
        when(proveedorService.crearProveedor("Textil", "30-11111111-1")).thenReturn(creado);

        String vista = controller.createProvider("30-11111111-1", "Textil", model);

        assertEquals("redirect:/admin/providers?success=created", vista);
    }

    @Test
    void createProvider_errorValidacion_retornaFormularioConError() {
        when(proveedorService.crearProveedor(any(), any()))
                .thenThrow(new IllegalArgumentException("El CUIT ya existe"));

        String vista = controller.createProvider("30-11111111-1", "Textil", model);

        assertEquals("admin/provider-form", vista);
        verify(model).addAttribute("errorMessage", "El CUIT ya existe");
        verify(model).addAttribute("isEdit", false);
    }

    @Test
    void showEditForm_proveedorExistente_retornaVistaEdicion() {
        Proveedor p = Proveedor.builder().id("p1").razonSocial("Textil").cuit("30-11111111-1").build();
        when(proveedorService.buscarPorId("p1")).thenReturn(p);

        String vista = controller.showEditForm("p1", model);

        assertEquals("admin/provider-form", vista);
        verify(model).addAttribute("proveedor", p);
        verify(model).addAttribute("isEdit", true);
    }

    @Test
    void showEditForm_proveedorInexistente_redirigeConError() {
        when(proveedorService.buscarPorId("no-existe"))
                .thenThrow(new IllegalArgumentException("Proveedor no encontrado"));

        String vista = controller.showEditForm("no-existe", model);

        assertEquals("redirect:/admin/providers?error=Proveedor no encontrado", vista);
    }

    @Test
    void updateProvider_exitoso_actualizaYRedirige() {
        Proveedor p = Proveedor.builder().id("p1").build();
        when(proveedorService.modificarProveedor("p1", "Nueva Textil", "30-22222222-2")).thenReturn(p);

        String vista = controller.updateProvider("p1", "30-22222222-2", "Nueva Textil", model);

        assertEquals("redirect:/admin/providers?success=updated", vista);
    }

    @Test
    void updateProvider_error_retornaFormularioConError() {
        when(proveedorService.modificarProveedor(any(), any(), any()))
                .thenThrow(new IllegalArgumentException("CUIT duplicado"));

        String vista = controller.updateProvider("p1", "30-22222222-2", "Nueva Textil", model);

        assertEquals("admin/provider-form", vista);
        verify(model).addAttribute("errorMessage", "CUIT duplicado");
        verify(model).addAttribute("isEdit", true);
    }

    @Test
    void deleteProviderPost_eliminaYRedirige() {
        String vista = controller.deleteProviderPost("p1");

        assertEquals("redirect:/admin/providers?success=deleted", vista);
        verify(proveedorService).eliminarProveedor("p1");
    }

    @Test
    void deleteProviderGet_eliminaYRedirige() {
        String vista = controller.deleteProviderGet("p1");

        assertEquals("redirect:/admin/providers?success=deleted", vista);
        verify(proveedorService).eliminarProveedor("p1");
    }
}

