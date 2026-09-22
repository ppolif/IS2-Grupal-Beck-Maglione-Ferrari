package com.example.zero.services;

import com.example.zero.entidades.compraProveedor.Proveedor;
import com.example.zero.repositories.ProveedorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProveedorServiceTest {

    @Mock
    private ProveedorRepository proveedorRepository;

    @InjectMocks
    private ProveedorService proveedorService;

    @Test
    void validar_camposValidos_noLanzaExcepcion() {
        assertDoesNotThrow(() -> proveedorService.validar("Textil S.A.", "30-11223344-5"));
    }

    @Test
    void validar_razonSocialVacia_lanzaExcepcion() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> proveedorService.validar("", "30-11223344-5"));
        assertTrue(ex.getMessage().contains("razón social"));
    }

    @Test
    void validar_cuitVacio_lanzaExcepcion() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> proveedorService.validar("Textil S.A.", "  "));
        assertTrue(ex.getMessage().contains("CUIT"));
    }

    @Test
    void crearProveedor_datosValidos_creaYGuarda() {
        when(proveedorRepository.findByCuitAndEliminadoFalse("30-11223344-5")).thenReturn(Optional.empty());
        when(proveedorRepository.save(any(Proveedor.class))).thenAnswer(inv -> inv.getArgument(0));

        Proveedor resultado = proveedorService.crearProveedor("Textil S.A.", "30-11223344-5");

        assertNotNull(resultado);
        assertEquals("Textil S.A.", resultado.getRazonSocial());
        assertEquals("30-11223344-5", resultado.getCuit());
        assertFalse(resultado.isEliminado());
        verify(proveedorRepository).save(any(Proveedor.class));
    }

    @Test
    void crearProveedor_cuitDuplicadoActivo_lanzaExcepcion() {
        Proveedor existente = Proveedor.builder().cuit("30-11223344-5").build();
        when(proveedorRepository.findByCuitAndEliminadoFalse("30-11223344-5")).thenReturn(Optional.of(existente));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> proveedorService.crearProveedor("Textil S.A.", "30-11223344-5"));
        assertTrue(ex.getMessage().contains("Ya existe un proveedor activo"));
        verify(proveedorRepository, never()).save(any());
    }

    @Test
    void modificarProveedor_datosValidos_actualizaYGuarda() {
        Proveedor existente = Proveedor.builder().id("p1").razonSocial("Vieja S.A.").cuit("30-11111111-1").build();
        when(proveedorRepository.findActive("p1")).thenReturn(Optional.of(existente));
        when(proveedorRepository.findByCuitAndEliminadoFalse("30-22222222-2")).thenReturn(Optional.empty());
        when(proveedorRepository.save(any(Proveedor.class))).thenAnswer(inv -> inv.getArgument(0));

        Proveedor modificado = proveedorService.modificarProveedor("p1", "Nueva S.A.", "30-22222222-2");

        assertEquals("Nueva S.A.", modificado.getRazonSocial());
        assertEquals("30-22222222-2", modificado.getCuit());
        verify(proveedorRepository).save(existente);
    }

    @Test
    void modificarProveedor_cuitDuplicadoEnOtroProveedor_lanzaExcepcion() {
        Proveedor existente = Proveedor.builder().id("p1").razonSocial("Mia S.A.").cuit("30-11111111-1").build();
        Proveedor otro = Proveedor.builder().id("p2").cuit("30-22222222-2").build();
        when(proveedorRepository.findActive("p1")).thenReturn(Optional.of(existente));
        when(proveedorRepository.findByCuitAndEliminadoFalse("30-22222222-2")).thenReturn(Optional.of(otro));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> proveedorService.modificarProveedor("p1", "Mia S.A.", "30-22222222-2"));
        assertTrue(ex.getMessage().contains("Ya existe otro proveedor activo"));
        verify(proveedorRepository, never()).save(existente);
    }

    @Test
    void eliminarProveedor_existente_marcaEliminadoTrue() {
        Proveedor existente = Proveedor.builder().id("p1").eliminado(false).build();
        when(proveedorRepository.findActive("p1")).thenReturn(Optional.of(existente));
        when(proveedorRepository.save(any(Proveedor.class))).thenAnswer(inv -> inv.getArgument(0));

        proveedorService.eliminarProveedor("p1");

        assertTrue(existente.isEliminado());
        verify(proveedorRepository).save(existente);
    }

    @Test
    void buscarPorId_existente_retornaProveedor() {
        Proveedor p = Proveedor.builder().id("p1").build();
        when(proveedorRepository.findActive("p1")).thenReturn(Optional.of(p));

        Proveedor encontrado = proveedorService.buscarPorId("p1");

        assertEquals(p, encontrado);
    }

    @Test
    void buscarPorId_inexistente_lanzaExcepcion() {
        when(proveedorRepository.findActive("inexistente")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> proveedorService.buscarPorId("inexistente"));
    }

    @Test
    void listarActivos_retornaLista() {
        List<Proveedor> lista = List.of(Proveedor.builder().id("p1").build());
        when(proveedorRepository.findByEliminadoFalse()).thenReturn(lista);

        List<Proveedor> resultado = proveedorService.listarActivos();

        assertEquals(1, resultado.size());
    }
}

