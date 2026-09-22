package com.example.zero.services;

import com.example.zero.dto.persona.ClienteRegistroDTO;
import com.example.zero.entidades.empresa.Contacto;
import com.example.zero.entidades.empresa.ContactoCorreoElectronico;
import com.example.zero.entidades.empresa.ContactoTelefonico;
import com.example.zero.entidades.persona.Cliente;
import com.example.zero.entidades.persona.Nacionalidad;
import com.example.zero.entidades.persona.Usuario;
import com.example.zero.entidades.zona.Direccion;
import com.example.zero.entidades.zona.Localidad;
import com.example.zero.enums.RolUsuario;
import com.example.zero.enums.TipoDocumento;
import com.example.zero.enums.TipoTelefono;
import com.example.zero.repositories.*;
import com.example.zero.services.persona.ClienteService;
import com.example.zero.services.persona.UsuarioService;
import com.example.zero.services.zona.ZonaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceRegistroTest {

    @Mock private ClienteRepository clienteRepository;
    @Mock private NacionalidadRepository nacionalidadRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private UsuarioService usuarioService;
    @Mock private ZonaService zonaService;
    @Mock private DireccionRepository direccionRepository;
    @Mock private ContactoRepository contactoRepository;

    private ClienteService clienteService;

    @BeforeEach
    void setUp() {
        clienteService = new ClienteService(
                clienteRepository,
                nacionalidadRepository,
                usuarioRepository,
                usuarioService,
                zonaService,
                direccionRepository,
                contactoRepository
        );
    }

    @Test
    void registrarCliente_conContactoEmail_exitoso() {
        ClienteRegistroDTO dto = ClienteRegistroDTO.builder()
                .email("cliente@test.com")
                .password("pass1234")
                .confirmPassword("pass1234")
                .nombre("Juan")
                .apellido("Pérez")
                .tipoDocumento(TipoDocumento.DNI)
                .numeroDocumento("40123456")
                .fechaNacimiento(LocalDate.of(1998, 4, 15))
                .nacionalidadId("nac-01")
                .tipoContacto("EMAIL")
                .contactoEmail("contacto@test.com")
                .contactoObservacion("Contacto preferente")
                .calle("Av. Colón")
                .numeracion("1234")
                .localidadId("loc-cba-cba")
                .build();

        Nacionalidad nac = Nacionalidad.builder().id("nac-01").nombre("Argentina").build();
        Localidad loc = new Localidad(); loc.setId("loc-cba-cba"); loc.setNombre("Córdoba Ciudad");
        Usuario usuarioMock = Usuario.builder().id("usr-1").nombreUsuario("cliente@test.com").rol(RolUsuario.CLIENTE).build();

        when(usuarioRepository.findByNombreUsuarioAndEliminadoFalse("cliente@test.com")).thenReturn(Optional.empty());
        when(clienteRepository.findByNumeroDocumentoAndEliminadoFalse("40123456")).thenReturn(Optional.empty());
        when(nacionalidadRepository.findActive("nac-01")).thenReturn(Optional.of(nac));
        when(zonaService.buscarLocalidadPorId("loc-cba-cba")).thenReturn(loc);
        when(direccionRepository.save(any(Direccion.class))).thenAnswer(i -> i.getArgument(0));
        when(contactoRepository.save(any(Contacto.class))).thenAnswer(i -> i.getArgument(0));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(i -> i.getArgument(0));
        when(usuarioService.crearUsuario(eq("cliente@test.com"), eq("pass1234"), eq(RolUsuario.CLIENTE), any(Cliente.class)))
                .thenReturn(usuarioMock);

        Usuario resultado = clienteService.registrarCliente(dto);

        assertNotNull(resultado);
        assertEquals("cliente@test.com", resultado.getNombreUsuario());
        assertEquals(RolUsuario.CLIENTE, resultado.getRol());

        verify(direccionRepository, times(1)).save(any(Direccion.class));
        verify(contactoRepository, times(1)).save(any(ContactoCorreoElectronico.class));
        verify(clienteRepository, times(2)).save(any(Cliente.class));
        verify(usuarioService, times(1)).crearUsuario(eq("cliente@test.com"), eq("pass1234"), eq(RolUsuario.CLIENTE), any(Cliente.class));
    }

    @Test
    void registrarCliente_conContactoCelular_exitoso() {
        ClienteRegistroDTO dto = ClienteRegistroDTO.builder()
                .email("maria@test.com")
                .password("pass1234")
                .confirmPassword("pass1234")
                .nombre("María")
                .apellido("Gómez")
                .tipoDocumento(TipoDocumento.DNI)
                .numeroDocumento("35987654")
                .fechaNacimiento(LocalDate.of(1992, 8, 20))
                .nacionalidadId("nac-01")
                .tipoContacto("CELULAR")
                .contactoTelefono("3519876543")
                .contactoObservacion("Llamar por la tarde")
                .calle("San Martín")
                .numeracion("456")
                .localidadId("loc-cba-cba")
                .build();

        Nacionalidad nac = Nacionalidad.builder().id("nac-01").nombre("Argentina").build();
        Localidad loc = new Localidad(); loc.setId("loc-cba-cba"); loc.setNombre("Córdoba Ciudad");
        Usuario usuarioMock = Usuario.builder().id("usr-2").nombreUsuario("maria@test.com").rol(RolUsuario.CLIENTE).build();

        when(usuarioRepository.findByNombreUsuarioAndEliminadoFalse("maria@test.com")).thenReturn(Optional.empty());
        when(clienteRepository.findByNumeroDocumentoAndEliminadoFalse("35987654")).thenReturn(Optional.empty());
        when(nacionalidadRepository.findActive("nac-01")).thenReturn(Optional.of(nac));
        when(zonaService.buscarLocalidadPorId("loc-cba-cba")).thenReturn(loc);
        when(direccionRepository.save(any(Direccion.class))).thenAnswer(i -> i.getArgument(0));
        when(contactoRepository.save(any(Contacto.class))).thenAnswer(i -> i.getArgument(0));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(i -> i.getArgument(0));
        when(usuarioService.crearUsuario(eq("maria@test.com"), eq("pass1234"), eq(RolUsuario.CLIENTE), any(Cliente.class)))
                .thenReturn(usuarioMock);

        Usuario resultado = clienteService.registrarCliente(dto);

        assertNotNull(resultado);
        verify(contactoRepository, times(1)).save(any(ContactoTelefonico.class));
    }

    @Test
    void registrarCliente_conPasswordNoCoincide_lanzaExcepcion() {
        ClienteRegistroDTO dto = ClienteRegistroDTO.builder()
                .email("test@test.com")
                .password("clave123")
                .confirmPassword("otraClave")
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> clienteService.registrarCliente(dto));
        assertEquals("Las contraseñas no coinciden", ex.getMessage());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void registrarCliente_conDocumentoDuplicado_lanzaExcepcion() {
        ClienteRegistroDTO dto = ClienteRegistroDTO.builder()
                .email("test@test.com")
                .password("clave123")
                .confirmPassword("clave123")
                .nombre("Juan")
                .apellido("Pérez")
                .numeroDocumento("40123456")
                .fechaNacimiento(LocalDate.of(2000, 1, 1))
                .build();

        when(usuarioRepository.findByNombreUsuarioAndEliminadoFalse("test@test.com")).thenReturn(Optional.empty());
        when(clienteRepository.findByNumeroDocumentoAndEliminadoFalse("40123456"))
                .thenReturn(Optional.of(Cliente.builder().numeroDocumento("40123456").build()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> clienteService.registrarCliente(dto));
        assertTrue(ex.getMessage().contains("Ya existe un cliente activo con el documento"));
        verify(usuarioService, never()).crearUsuario(any(), any(), any(), any());
    }

    @Test
    void registrarCliente_conEmailDuplicado_lanzaExcepcion() {
        ClienteRegistroDTO dto = ClienteRegistroDTO.builder()
                .email("existente@test.com")
                .password("clave123")
                .confirmPassword("clave123")
                .build();

        when(usuarioRepository.findByNombreUsuarioAndEliminadoFalse("existente@test.com"))
                .thenReturn(Optional.of(Usuario.builder().nombreUsuario("existente@test.com").build()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> clienteService.registrarCliente(dto));
        assertTrue(ex.getMessage().contains("Ya existe un usuario activo con el correo"));
    }
}

