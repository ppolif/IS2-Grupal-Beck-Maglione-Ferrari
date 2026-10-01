package com.example.zero.services;

import com.example.zero.dto.persona.ClienteRegistroDTO;
import com.example.zero.entidades.Imagen;
import com.example.zero.entidades.empresa.ContactoCorreoElectronico;
import com.example.zero.entidades.empresa.ContactoTelefonico;
import com.example.zero.entidades.persona.Cliente;
import com.example.zero.entidades.persona.Nacionalidad;
import com.example.zero.entidades.persona.Usuario;
import com.example.zero.entidades.zona.Direccion;
import com.example.zero.enums.RolUsuario;
import com.example.zero.enums.TipoContacto;
import com.example.zero.enums.TipoDocumento;
import com.example.zero.enums.TipoImagen;
import com.example.zero.enums.TipoTelefono;
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
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceRegistroTest {

    @Mock private ClienteRepository clienteRepository;
    @Mock private NacionalidadService nacionalidadService;
    @Mock private UsuarioService usuarioService;
    @Mock private ZonaService zonaService;
    @Mock private ContactoService contactoService;
    @Mock private ImagenService imagenService;

    @InjectMocks
    private ClienteService clienteService;

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
        Direccion dir = Direccion.builder().id("dir-1").calle("Av. Colón").build();
        ContactoCorreoElectronico contactoMail = ContactoCorreoElectronico.builder().id("ct-1").email("contacto@test.com").build();
        Usuario usuarioMock = Usuario.builder().id("usr-1").nombreUsuario("cliente@test.com").rol(RolUsuario.CLIENTE).build();

        when(usuarioService.existePorNombreUsuario("cliente@test.com")).thenReturn(false);
        when(clienteRepository.findByNumeroDocumentoAndEliminadoFalse("40123456")).thenReturn(Optional.empty());
        when(nacionalidadService.buscarPorId("nac-01")).thenReturn(nac);
        when(zonaService.crearDireccion(any(), any(), any(), any(), any(), any(), any())).thenReturn(dir);
        when(contactoService.crearContactoCorreo(eq("contacto@test.com"), eq(TipoContacto.PERSONAL), eq("Contacto preferente"))).thenReturn(contactoMail);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(i -> i.getArgument(0));
        when(usuarioService.crearUsuario(eq("cliente@test.com"), eq("pass1234"), eq(RolUsuario.CLIENTE), any(Cliente.class), eq(false)))
                .thenReturn(usuarioMock);
        when(usuarioService.generarYAsignarCodigo("cliente@test.com")).thenReturn("123456");

        Usuario resultado = clienteService.registrarCliente(dto);

        assertNotNull(resultado);
        assertEquals("cliente@test.com", resultado.getNombreUsuario());
        assertEquals(RolUsuario.CLIENTE, resultado.getRol());

        verify(zonaService, times(1)).crearDireccion(any(), any(), any(), any(), any(), any(), any());
        verify(contactoService, times(1)).crearContactoCorreo(eq("contacto@test.com"), eq(TipoContacto.PERSONAL), eq("Contacto preferente"));
        verify(clienteRepository, times(1)).save(any(Cliente.class));
        verify(usuarioService, times(1)).crearUsuario(eq("cliente@test.com"), eq("pass1234"), eq(RolUsuario.CLIENTE), any(Cliente.class), eq(false));
        verify(usuarioService, times(1)).generarYAsignarCodigo("cliente@test.com");
        verify(usuarioService, times(1)).enviarCodigoConfirmacion("cliente@test.com", "123456");
        verify(imagenService, times(1)).guardarMonigoteDefault();
    }

    @Test
    void registrarCliente_conFotoPerfil_guardaImagenTipoPersona() {
        ClienteRegistroDTO dto = ClienteRegistroDTO.builder()
                .email("foto@test.com")
                .password("pass1234")
                .confirmPassword("pass1234")
                .nombre("Juan")
                .apellido("Pérez")
                .tipoDocumento(TipoDocumento.DNI)
                .numeroDocumento("40123457")
                .fechaNacimiento(LocalDate.of(1998, 4, 15))
                .nacionalidadId("nac-01")
                .tipoContacto("EMAIL")
                .contactoEmail("foto@test.com")
                .calle("Av. Colón")
                .numeracion("1234")
                .localidadId("loc-cba-cba")
                .build();

        MockMultipartFile file = new MockMultipartFile("fotoPerfil", "foto.png", "image/png", new byte[]{1, 2, 3});
        Imagen imagenMock = Imagen.builder().id("img-persona-1").tipoImagen(TipoImagen.PERSONA).build();
        when(imagenService.guardarImagen(file, TipoImagen.PERSONA)).thenReturn(imagenMock);

        Nacionalidad nac = Nacionalidad.builder().id("nac-01").nombre("Argentina").build();
        Direccion dir = Direccion.builder().id("dir-1").build();
        ContactoCorreoElectronico contactoMail = ContactoCorreoElectronico.builder().id("ct-1").email("foto@test.com").build();
        Usuario usuarioMock = Usuario.builder().id("usr-foto").nombreUsuario("foto@test.com").rol(RolUsuario.CLIENTE).build();

        when(usuarioService.existePorNombreUsuario("foto@test.com")).thenReturn(false);
        when(clienteRepository.findByNumeroDocumentoAndEliminadoFalse("40123457")).thenReturn(Optional.empty());
        when(nacionalidadService.buscarPorId("nac-01")).thenReturn(nac);
        when(zonaService.crearDireccion(any(), any(), any(), any(), any(), any(), any())).thenReturn(dir);
        when(contactoService.crearContactoCorreo(any(), any(), any())).thenReturn(contactoMail);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(i -> i.getArgument(0));
        when(usuarioService.crearUsuario(eq("foto@test.com"), eq("pass1234"), eq(RolUsuario.CLIENTE), any(Cliente.class), eq(false)))
                .thenReturn(usuarioMock);
        when(usuarioService.actualizarFoto(any(), eq("/imagen/img-persona-1"))).thenReturn(usuarioMock);

        Usuario resultado = clienteService.registrarCliente(dto, file);

        assertNotNull(resultado);
        verify(imagenService, times(1)).guardarImagen(file, TipoImagen.PERSONA);
        verify(imagenService, never()).guardarMonigoteDefault();
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
        Direccion dir = Direccion.builder().id("dir-2").build();
        ContactoTelefonico contactoTel = ContactoTelefonico.builder().id("ct-2").telefono("3519876543").build();
        Usuario usuarioMock = Usuario.builder().id("usr-2").nombreUsuario("maria@test.com").rol(RolUsuario.CLIENTE).build();

        when(usuarioService.existePorNombreUsuario("maria@test.com")).thenReturn(false);
        when(clienteRepository.findByNumeroDocumentoAndEliminadoFalse("35987654")).thenReturn(Optional.empty());
        when(nacionalidadService.buscarPorId("nac-01")).thenReturn(nac);
        when(zonaService.crearDireccion(any(), any(), any(), any(), any(), any(), any())).thenReturn(dir);
        when(contactoService.crearContactoTelefonico(eq("3519876543"), eq(TipoTelefono.CELULAR), eq(TipoContacto.PERSONAL), eq("Llamar por la tarde")))
                .thenReturn(contactoTel);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(i -> i.getArgument(0));
        when(usuarioService.crearUsuario(eq("maria@test.com"), eq("pass1234"), eq(RolUsuario.CLIENTE), any(Cliente.class), eq(false)))
                .thenReturn(usuarioMock);

        Usuario resultado = clienteService.registrarCliente(dto);

        assertNotNull(resultado);
        verify(contactoService, times(1)).crearContactoTelefonico(eq("3519876543"), eq(TipoTelefono.CELULAR), eq(TipoContacto.PERSONAL), eq("Llamar por la tarde"));
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
    void registrarCliente_conEmailDuplicado_lanzaExcepcion() {
        ClienteRegistroDTO dto = ClienteRegistroDTO.builder()
                .email("existente@test.com")
                .password("clave123")
                .confirmPassword("clave123")
                .build();

        when(usuarioService.existePorNombreUsuario("existente@test.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> clienteService.registrarCliente(dto));
        assertTrue(ex.getMessage().contains("Ya existe un usuario activo con el correo"));
    }
}
