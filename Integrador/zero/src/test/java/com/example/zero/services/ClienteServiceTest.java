package com.example.zero.services;

import com.example.zero.entidades.persona.Cliente;
import com.example.zero.entidades.persona.Nacionalidad;
import com.example.zero.entidades.persona.Usuario;
import com.example.zero.enums.TipoDocumento;
import com.example.zero.entidades.Imagen;
import com.example.zero.repositories.ClienteRepository;
import com.example.zero.services.persona.ClienteService;
import com.example.zero.services.persona.NacionalidadService;
import com.example.zero.services.persona.UsuarioService;
import com.example.zero.services.zona.ZonaService;
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

    @Mock
    private NacionalidadService nacionalidadService;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private ZonaService zonaService;

    @Mock
    private ContactoService contactoService;

    @Mock
    private ImagenService imagenService;

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
                "12345678", "Juan", "Perez", null, TipoDocumento.DNI, null
        ));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void crearCliente_conCamposVacios_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> clienteService.crearCliente("", "Juan", "Perez", null, TipoDocumento.DNI, null));
        assertThrows(IllegalArgumentException.class, () -> clienteService.crearCliente("123", "", "Perez", null, TipoDocumento.DNI, null));
        assertThrows(IllegalArgumentException.class, () -> clienteService.crearCliente("123", "Juan", "", null, TipoDocumento.DNI, null));
        assertThrows(IllegalArgumentException.class, () -> clienteService.crearCliente("123", "Juan", "Perez", null, null, null));
    }

    @Test
    void modificarCliente_conDatosValidos_actualizaYRetorna() {
        Cliente cliente = Cliente.builder().numeroDocumento("12345678").nombre("Juan").apellido("Perez").tipoDocumento(TipoDocumento.DNI).build();
        when(clienteRepository.findByNumeroDocumentoAndEliminadoFalse("12345678")).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        Cliente modificado = clienteService.modificarCliente("12345678", "Juan Carlos", "Perez Gomez", null, TipoDocumento.DNI, null);

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
        when(usuarioService.asociarPersona(usuario, cliente)).thenReturn(usuario);

        Usuario actualizado = clienteService.asociarClienteUsuario("12345678", usuario);

        assertNotNull(actualizado);
        verify(usuarioService, times(1)).asociarPersona(usuario, cliente);
    }

    @Test
    void obtenerFotoPerfilCliente_conUsuarioAsociadoConFoto_retornaFotoUsuario() {
        Cliente cliente = Cliente.builder().numeroDocumento("12345678").build();
        when(usuarioService.obtenerFotoPerfilPorDocumento("12345678")).thenReturn("https://ejemplo.com/mifoto.jpg");

        String foto = clienteService.obtenerFotoPerfilCliente(cliente);

        assertEquals("https://ejemplo.com/mifoto.jpg", foto);
    }

    @Test
    void obtenerFotoPerfilCliente_sinUsuarioPeroConImagen_retornaRutaImagen() {
        Imagen img = Imagen.builder().id("img-99").eliminado(false).build();
        Cliente cliente = Cliente.builder().numeroDocumento("12345678").imagen(List.of(img)).build();
        when(usuarioService.obtenerFotoPerfilPorDocumento("12345678")).thenReturn(null);

        String foto = clienteService.obtenerFotoPerfilCliente(cliente);

        assertEquals("/imagen/img-99", foto);
    }

    @Test
    void obtenerFotoPerfilCliente_sinDatos_retornaAvatarPorDefecto() {
        String fotoNull = clienteService.obtenerFotoPerfilCliente(null);
        assertEquals("/admin/assets/images/avatar.png", fotoNull);

        Cliente cliente = Cliente.builder().numeroDocumento("12345678").build();
        when(usuarioService.obtenerFotoPerfilPorDocumento("12345678")).thenReturn(null);

        String fotoDefecto = clienteService.obtenerFotoPerfilCliente(cliente);
        assertEquals("/admin/assets/images/avatar.png", fotoDefecto);
    }
}
