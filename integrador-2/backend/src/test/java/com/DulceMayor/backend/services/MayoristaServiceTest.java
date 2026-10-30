package com.DulceMayor.backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.DulceMayor.backend.config.ConflictoException;
import com.DulceMayor.backend.config.ValidacionException;
import com.DulceMayor.backend.dtos.mayorista.ConfiguracionCorreoMayoristaMapper;
import com.DulceMayor.backend.dtos.mayorista.ConfiguracionCorreoMayoristaRequest;
import com.DulceMayor.backend.dtos.mayorista.ConfiguracionCorreoMayoristaResponse;
import com.DulceMayor.backend.dtos.mayorista.MayoristaMapper;
import com.DulceMayor.backend.dtos.mayorista.MayoristaRequest;
import com.DulceMayor.backend.dtos.mayorista.MayoristaResponse;
import com.DulceMayor.backend.entities.Imagen;
import com.DulceMayor.backend.entities.TipoImagen;
import com.DulceMayor.backend.entities.mayorista.ConfiguracionCorreoMayorista;
import com.DulceMayor.backend.entities.mayorista.Mayorista;
import com.DulceMayor.backend.entities.mayorista.TipoEmpresa;
import com.DulceMayor.backend.repositories.mayorista.ConfiguracionCorreoMayoristaRepository;
import com.DulceMayor.backend.repositories.mayorista.MayoristaRepository;
import com.DulceMayor.backend.services.direccion.DireccionService;
import com.DulceMayor.backend.services.mayorista.ConfiguracionCorreoMayoristaService;
import com.DulceMayor.backend.services.mayorista.MayoristaService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests unitarios para {@link MayoristaService} y {@link ConfiguracionCorreoMayoristaService}.
 */
@ExtendWith(MockitoExtension.class)
class MayoristaServiceTest {

    @Mock
    private MayoristaRepository mayoristaRepository;

    @Mock
    private MayoristaMapper mayoristaMapper;

    @Mock
    private DireccionService direccionService;

    @Mock
    private ContactoService contactoService;

    @Mock
    private ImagenService imagenService;

    @Mock
    private ConfiguracionCorreoMayoristaRepository configuracionCorreoMayoristaRepository;

    @Mock
    private ConfiguracionCorreoMayoristaMapper configuracionCorreoMayoristaMapper;

    private MayoristaService mayoristaService;
    private ConfiguracionCorreoMayoristaService configuracionCorreoMayoristaService;

    @BeforeEach
    void setUp() {
        mayoristaService = new MayoristaService(
                mayoristaRepository,
                mayoristaMapper,
                direccionService,
                contactoService,
                imagenService
        );

        configuracionCorreoMayoristaService = new ConfiguracionCorreoMayoristaService(
                configuracionCorreoMayoristaRepository,
                configuracionCorreoMayoristaMapper,
                mayoristaService
        );
    }

    @Test
    @DisplayName("crear: rechaza con 409 ConflictoException si el CUIT del mayorista ya existe")
    void crearMayoristaCuitDuplicadoLanza409() {
        MayoristaRequest req = new MayoristaRequest(
                "Dulce Mayor S.A.", "30-11223344-5", TipoEmpresa.SEDE_CENTRAL,
                List.of(), List.of(), List.of()
        );
        when(mayoristaRepository.existsByCuitAndEliminadoFalse("30-11223344-5")).thenReturn(true);

        assertThrows(ConflictoException.class, () -> mayoristaService.crear(req));
    }

    @Test
    @DisplayName("crear: rechaza con ValidacionException si se asocia una imagen de tipo PRODUCTO")
    void crearMayoristaImagenTipoInvalidoLanza400() {
        MayoristaRequest req = new MayoristaRequest(
                "Dulce Mayor Sucursal", "30-99887766-5", TipoEmpresa.SUCURSAL,
                List.of(), List.of(), List.of("img-prod")
        );
        when(mayoristaRepository.existsByCuitAndEliminadoFalse("30-99887766-5")).thenReturn(false);

        Imagen imgProd = Imagen.builder().id("img-prod").tipoImagen(TipoImagen.PRODUCTO).build();
        when(imagenService.obtenerEntidad("img-prod")).thenReturn(imgProd);

        assertThrows(ValidacionException.class, () -> mayoristaService.crear(req));
    }

    @Test
    @DisplayName("modificarPorMayorista: guarda configuración SMTP y devuelve respuesta sin clave")
    void configurarSmtpMayoristaExitoso() {
        Mayorista mayorista = Mayorista.builder().id("may-1").razonSocial("Dulce Mayor").build();
        when(mayoristaRepository.findByIdAndEliminadoFalse("may-1")).thenReturn(Optional.of(mayorista));

        ConfiguracionCorreoMayoristaRequest req = new ConfiguracionCorreoMayoristaRequest(
                "notificaciones@dulcemayor.com", "PasswordSecreta123", "587", "smtp.gmail.com", true
        );

        when(configuracionCorreoMayoristaRepository.findByMayoristaIdAndEliminadoFalse("may-1"))
                .thenReturn(Optional.empty());

        ConfiguracionCorreoMayorista entidad = ConfiguracionCorreoMayorista.builder()
                .correo("notificaciones@dulcemayor.com")
                .clave("PasswordSecreta123")
                .puerto("587")
                .smtp("smtp.gmail.com")
                .tls(true)
                .build();
        when(configuracionCorreoMayoristaMapper.aEntidad(any())).thenReturn(entidad);

        ConfiguracionCorreoMayorista guardada = ConfiguracionCorreoMayorista.builder()
                .id("cfg-1")
                .correo("notificaciones@dulcemayor.com")
                .clave("PasswordSecreta123")
                .puerto("587")
                .smtp("smtp.gmail.com")
                .tls(true)
                .mayorista(mayorista)
                .build();
        when(configuracionCorreoMayoristaRepository.save(any())).thenReturn(guardada);

        ConfiguracionCorreoMayoristaResponse mockRes = new ConfiguracionCorreoMayoristaResponse(
                "cfg-1", "notificaciones@dulcemayor.com", "587", "smtp.gmail.com", true
        );
        when(configuracionCorreoMayoristaMapper.aResponse(guardada)).thenReturn(mockRes);

        ConfiguracionCorreoMayoristaResponse res = configuracionCorreoMayoristaService.modificarPorMayorista("may-1", req);

        assertNotNull(res);
        assertEquals("cfg-1", res.id());
        assertEquals("notificaciones@dulcemayor.com", res.correo());
        verify(configuracionCorreoMayoristaRepository).save(any());
    }
}

