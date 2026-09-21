package com.example.zero.services;

import com.example.zero.entidades.persona.Cliente;
import com.example.zero.entidades.persona.Nacionalidad;
import com.example.zero.entidades.persona.Usuario;
import com.example.zero.enums.TipoDocumento;
import com.example.zero.repositories.ClienteRepository;
import com.example.zero.services.persona.ClienteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    @Test
    void crearCliente_conDatosValidos_guardaYRetornaCliente() {
        Nacionalidad nac = Nacionalidad.builder().id("nac-1").nombre("Argentina").build();
        when(clienteRepository.findByNumeroDocumentoAndEliminadoFalse("12345678")).thenReturn(Optional.empty());
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        Cliente resultado = clienteService.crearCliente(
                "12345678", "Juan", "Perez", LocalDate.of(1995, 5, 10), TipoDocumento.DNI, nac
        );

        assertNotNull(resultado);
        assertEquals("12345678", resultado.getNumeroDocumento());
        assertEquals("Juan", resultado.getNombre());
        assertEquals("Perez", resultado.getApellido());
        assertFalse(resultado.isEliminado());
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    void crearCliente_conDocumentoDuplicado_lanzaIllegalArgumentException() {
        when(clienteRepository.findByNumeroDocumentoAndEliminadoFalse("12345678"))
                .thenReturn(Optional.of(Cliente.builder().numeroDocumento("12345678").build()));

        assertThrows(IllegalArgumentException.class, () -> clienteService.crearCliente(
                "12345678", "Juan", "Perez", null, null, null
        ));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void crearCliente_conCamposVacios_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> clienteService.crearCliente("", "Juan", "Perez", null, null, null));
        assertThrows(IllegalArgumentException.class, () -> clienteService.crearCliente("123", "", "Perez", null, null, null));
        assertThrows(IllegalArgumentException.class, () -> clienteService.crearCliente("123", "Juan", "", null, null, null));
    }

    @Test
    void modificarCliente_conDatosValidos_actualizaYRetorna() {
        Cliente cliente = Cliente.builder().numeroDocumento("12345678").nombre("Juan").apellido("Perez").build();
        when(clienteRepository.findByNumeroDocumentoAndEliminadoFalse("12345678")).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        Cliente modificado = clienteService.modificarCliente("12345678", "Juan Carlos", "Perez Gomez", null, null, null);

        assertEquals("Juan Carlos", modificado.getNombre());
        assertEquals("Perez Gomez", modificado.getApellido());
        verify(clienteRepository, times(1)).save(cliente);
    }

    @Test
    void eliminarCliente_existente_marcaEliminado() {
        Cliente cliente = Cliente.builder().numeroDocumento("12345678").eliminado(false).build();
        when(clienteRepository.findByNumeroDocumentoAndEliminadoFalse("12345678")).thenReturn(Optional.of(cliente));

        clienteService.eliminarCliente("12345678");

        assertTrue(cliente.isEliminado());
        verify(clienteRepository, times(1)).save(cliente);
    }

    @Test
    void buscarPorDocumento_existente_retornaCliente() {
        Cliente cliente = Cliente.builder().numeroDocumento("12345678").nombre("Maria").build();
        when(clienteRepository.findByNumeroDocumentoAndEliminadoFalse("12345678")).thenReturn(Optional.of(cliente));

        Cliente encontrado = clienteService.buscarPorDocumento("12345678");

        assertEquals("Maria", encontrado.getNombre());
    }

    @Test
    void asociarClienteUsuario_asociaYGuarda() {
        Cliente cliente = Cliente.builder().numeroDocumento("12345678").build();
        Usuario usuario = Usuario.builder().id("u-1").nombreUsuario("juan@zero.com").build();
        when(clienteRepository.findByNumeroDocumentoAndEliminadoFalse("12345678")).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        Cliente actualizado = clienteService.asociarClienteUsuario("12345678", usuario);

        assertEquals(usuario, actualizado.getUsuario());
        verify(clienteRepository, times(1)).save(cliente);
    }
}

