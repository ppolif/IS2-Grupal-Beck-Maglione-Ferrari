package com.DulceMayor.backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.DulceMayor.backend.config.ConflictoException;
import com.DulceMayor.backend.dtos.UsuarioResponse;
import com.DulceMayor.backend.dtos.persona.EmpleadoMapper;
import com.DulceMayor.backend.dtos.persona.EmpleadoRequest;
import com.DulceMayor.backend.dtos.persona.EmpleadoResponse;
import com.DulceMayor.backend.dtos.persona.RolEmpleado;
import com.DulceMayor.backend.entities.ProveedorAuth;
import com.DulceMayor.backend.entities.RolUsuario;
import com.DulceMayor.backend.entities.Usuario;
import com.DulceMayor.backend.entities.mayorista.Mayorista;
import com.DulceMayor.backend.entities.mayorista.TipoEmpresa;
import com.DulceMayor.backend.entities.persona.Distribuidor;
import com.DulceMayor.backend.entities.persona.Empleado;
import com.DulceMayor.backend.entities.persona.Preventor;
import com.DulceMayor.backend.entities.persona.TipoDocumento;
import com.DulceMayor.backend.repositories.persona.EmpleadoRepository;
import com.DulceMayor.backend.services.direccion.DireccionService;
import com.DulceMayor.backend.services.mayorista.MayoristaService;
import com.DulceMayor.backend.services.persona.EmpleadoService;
import com.DulceMayor.backend.services.persona.PersonaService;
import com.DulceMayor.backend.services.ruta.strategy.RutaOptimizerStrategy;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests unitarios para {@link EmpleadoService} con Mockito.
 */
@ExtendWith(MockitoExtension.class)
class EmpleadoServiceTest {

    @Mock
    private EmpleadoRepository empleadoRepository;

    @Mock
    private EmpleadoMapper empleadoMapper;

    @Mock
    private PersonaService personaService;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private DireccionService direccionService;

    @Mock
    private ContactoService contactoService;

    @Mock
    private MayoristaService mayoristaService;

    @Mock
    private RutaOptimizerStrategy rutaOptimizerStrategy;

    private EmpleadoService empleadoService;

    @BeforeEach
    void setUp() {
        empleadoService = new EmpleadoService(
                empleadoRepository,
                empleadoMapper,
                personaService,
                usuarioService,
                direccionService,
                contactoService,
                mayoristaService,
                rutaOptimizerStrategy
        );
    }

    @Test
    @DisplayName("crear: instancia Preventor cuando rol es PREVENTISTA y genera Usuario con claveTemporal=true")
    void crearPreventorInstanciaSubclaseYGeneraUsuario() {
        EmpleadoRequest req = new EmpleadoRequest(
                "Carlos",
                "Lopez",
                null,
                TipoDocumento.DNI,
                "35111222",
                List.of(),
                List.of(),
                "LEG-PREV-01",
                "clopez",
                RolEmpleado.PREVENTISTA,
                "may-1"
        );

        when(empleadoRepository.existsByLegajoAndEliminadoFalse("LEG-PREV-01")).thenReturn(false);
        when(usuarioService.existeNombreUsuario("clopez")).thenReturn(false);

        Mayorista mockMayorista = Mayorista.builder().id("may-1").razonSocial("Dulce Mayor Sede").tipoEmpresa(TipoEmpresa.SEDE_CENTRAL).build();
        when(mayoristaService.obtenerEntidad("may-1")).thenReturn(mockMayorista);

        Usuario user = Usuario.builder()
                .id("user-1")
                .nombreUsuario("clopez")
                .rol(RolUsuario.PREVENTISTA)
                .claveTemporal(true)
                .proveedorAuth(ProveedorAuth.LOCAL)
                .build();
        when(usuarioService.crearUsuarioConClavePorDefecto("clopez", RolUsuario.PREVENTISTA)).thenReturn(user);

        when(empleadoRepository.save(any(Empleado.class))).thenAnswer(inv -> {
            Empleado e = inv.getArgument(0);
            e.setId("emp-1");
            return e;
        });

        EmpleadoResponse mockRes = new EmpleadoResponse(
                "emp-1", "LEG-PREV-01", "Carlos", "Lopez", null,
                TipoDocumento.DNI, "35111222", List.of(), List.of(),
                new UsuarioResponse("user-1", "clopez", RolUsuario.PREVENTISTA, true, ProveedorAuth.LOCAL),
                "may-1", "Dulce Mayor Sede"
        );
        when(empleadoMapper.aResponse(any(Empleado.class))).thenReturn(mockRes);

        EmpleadoResponse res = empleadoService.crear(req);

        assertNotNull(res);
        assertEquals("LEG-PREV-01", res.legajo());
        assertEquals("may-1", res.mayoristaId());
        assertTrue(res.usuario().claveTemporal());
        verify(personaService).validarDocumentoUnico(TipoDocumento.DNI, "35111222");
        verify(mayoristaService).obtenerEntidad("may-1");
    }

    @Test
    @DisplayName("crear: rechaza con 409 ConflictoException si el legajo ya existe")
    void crearEmpleadoLegajoDuplicadoLanza409() {
        EmpleadoRequest req = new EmpleadoRequest(
                "Mario", "Perez", null, TipoDocumento.DNI, "30999888",
                List.of(), List.of(), "LEG-01", "mperez", RolEmpleado.ADMINISTRATIVO, "may-1"
        );
        when(empleadoRepository.existsByLegajoAndEliminadoFalse("LEG-01")).thenReturn(true);

        assertThrows(ConflictoException.class, () -> empleadoService.crear(req));
    }

    @Test
    @DisplayName("crear: rechaza si se intenta asignar un Distribuidor a una sucursal que no sea la Sede Central")
    void crearDistribuidorEnSucursalRechazaConValidacionException() {
        EmpleadoRequest req = new EmpleadoRequest(
                "Lucas", "Gomez", null, TipoDocumento.DNI, "32111222",
                List.of(), List.of(), "LEG-DIST-01", "lgomez", RolEmpleado.DISTRIBUIDOR, "suc-2"
        );
        Mayorista sucursal = Mayorista.builder().id("suc-2").tipoEmpresa(TipoEmpresa.SUCURSAL).build();
        when(empleadoRepository.existsByLegajoAndEliminadoFalse("LEG-DIST-01")).thenReturn(false);
        when(usuarioService.existeNombreUsuario("lgomez")).thenReturn(false);
        when(mayoristaService.obtenerEntidad("suc-2")).thenReturn(sucursal);

        com.DulceMayor.backend.config.ValidacionException ex = assertThrows(
                com.DulceMayor.backend.config.ValidacionException.class,
                () -> empleadoService.crear(req)
        );
        assertEquals("Los distribuidores solo pueden ser asignados a la sede central", ex.getMessage());
    }

    @Test
    @DisplayName("eliminar: marca eliminado en Empleado y revoca el Usuario asociado")
    void eliminarEmpleadoRevocaUsuario() {
        Usuario user = Usuario.builder().id("usr-1").build();
        Empleado emp = Empleado.builder().id("emp-1").usuario(user).eliminado(false).build();

        when(empleadoRepository.findByIdAndEliminadoFalse("emp-1")).thenReturn(Optional.of(emp));

        empleadoService.eliminar("emp-1");

        assertTrue(emp.isEliminado());
        verify(usuarioService).eliminarUsuario("usr-1");
        verify(empleadoRepository).save(emp);
    }
}

