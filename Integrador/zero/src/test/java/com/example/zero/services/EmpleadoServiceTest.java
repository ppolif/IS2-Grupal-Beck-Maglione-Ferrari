package com.example.zero.services;

import com.example.zero.entidades.persona.Empleado;
import com.example.zero.entidades.persona.Usuario;
import com.example.zero.enums.TipoDocumento;
import com.example.zero.enums.TipoEmpleado;
import com.example.zero.repositories.EmpleadoRepository;
import com.example.zero.services.persona.EmpleadoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmpleadoServiceTest {

    @Mock
    private EmpleadoRepository empleadoRepository;

    @InjectMocks
    private EmpleadoService empleadoService;

    @Test
    void crearEmpleado_conDatosValidos_guardaYRetornaEmpleado() {
        when(empleadoRepository.findByNumeroDocumentoAndEliminadoFalse("20345678")).thenReturn(Optional.empty());
        when(empleadoRepository.save(any(Empleado.class))).thenAnswer(inv -> inv.getArgument(0));

        Empleado resultado = empleadoService.crearEmpleado(
                "20345678", "Carlos", "Lopez", LocalDate.of(1988, 3, 15),
                TipoDocumento.DNI, TipoEmpleado.ADMINISTRATIVO, null
        );

        assertNotNull(resultado);
        assertEquals("20345678", resultado.getNumeroDocumento());
        assertEquals("Carlos", resultado.getNombre());
        assertEquals(TipoEmpleado.ADMINISTRATIVO, resultado.getTipoEmpleado());
        assertFalse(resultado.isEliminado());
        verify(empleadoRepository, times(1)).save(any(Empleado.class));
    }

    @Test
    void crearEmpleado_conDocumentoDuplicado_lanzaIllegalArgumentException() {
        when(empleadoRepository.findByNumeroDocumentoAndEliminadoFalse("20345678"))
                .thenReturn(Optional.of(Empleado.builder().numeroDocumento("20345678").build()));

        assertThrows(IllegalArgumentException.class, () -> empleadoService.crearEmpleado(
                "20345678", "Carlos", "Lopez", null, null, null, null
        ));
        verify(empleadoRepository, never()).save(any());
    }

    @Test
    void modificarEmpleado_conDatosValidos_actualizaYRetorna() {
        Empleado empleado = Empleado.builder().numeroDocumento("20345678").nombre("Carlos").tipoEmpleado(TipoEmpleado.ADMINISTRATIVO).build();
        when(empleadoRepository.findByNumeroDocumentoAndEliminadoFalse("20345678")).thenReturn(Optional.of(empleado));
        when(empleadoRepository.save(any(Empleado.class))).thenAnswer(inv -> inv.getArgument(0));

        Empleado modificado = empleadoService.modificarEmpleado("20345678", "Carlos Alberto", null, null, null, TipoEmpleado.JEFE, null);

        assertEquals("Carlos Alberto", modificado.getNombre());
        assertEquals(TipoEmpleado.JEFE, modificado.getTipoEmpleado());
        verify(empleadoRepository, times(1)).save(empleado);
    }

    @Test
    void eliminarEmpleado_existente_marcaEliminado() {
        Empleado empleado = Empleado.builder().numeroDocumento("20345678").eliminado(false).build();
        when(empleadoRepository.findByNumeroDocumentoAndEliminadoFalse("20345678")).thenReturn(Optional.of(empleado));

        empleadoService.eliminarEmpleado("20345678");

        assertTrue(empleado.isEliminado());
        verify(empleadoRepository, times(1)).save(empleado);
    }

    @Test
    void buscarPorDocumento_existente_retornaEmpleado() {
        Empleado empleado = Empleado.builder().numeroDocumento("20345678").nombre("Laura").build();
        when(empleadoRepository.findByNumeroDocumentoAndEliminadoFalse("20345678")).thenReturn(Optional.of(empleado));

        Empleado encontrado = empleadoService.buscarPorDocumento("20345678");

        assertEquals("Laura", encontrado.getNombre());
    }

    @Test
    void asociarEmpleadoUsuario_asociaYGuarda() {
        Empleado empleado = Empleado.builder().numeroDocumento("20345678").build();
        Usuario usuario = Usuario.builder().id("u-2").nombreUsuario("carlos@zero.com").build();
        when(empleadoRepository.findByNumeroDocumentoAndEliminadoFalse("20345678")).thenReturn(Optional.of(empleado));
        when(empleadoRepository.save(any(Empleado.class))).thenAnswer(inv -> inv.getArgument(0));

        Empleado actualizado = empleadoService.asociarEmpleadoUsuario("20345678", usuario);

        assertEquals(usuario, actualizado.getUsuario());
        verify(empleadoRepository, times(1)).save(empleado);
    }
}
