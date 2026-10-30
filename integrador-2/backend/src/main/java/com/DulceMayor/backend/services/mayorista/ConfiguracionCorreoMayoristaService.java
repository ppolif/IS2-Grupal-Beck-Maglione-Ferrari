package com.DulceMayor.backend.services.mayorista;

import com.DulceMayor.backend.config.RecursoNoEncontradoException;
import com.DulceMayor.backend.config.ValidacionException;
import com.DulceMayor.backend.dtos.mayorista.ConfiguracionCorreoMayoristaMapper;
import com.DulceMayor.backend.dtos.mayorista.ConfiguracionCorreoMayoristaRequest;
import com.DulceMayor.backend.dtos.mayorista.ConfiguracionCorreoMayoristaResponse;
import com.DulceMayor.backend.entities.mayorista.ConfiguracionCorreoMayorista;
import com.DulceMayor.backend.entities.mayorista.Mayorista;
import com.DulceMayor.backend.repositories.mayorista.ConfiguracionCorreoMayoristaRepository;
import com.DulceMayor.backend.services.BaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de negocio para la configuración de correo saliente SMTP de una empresa mayorista.
 */
@Service
public class ConfiguracionCorreoMayoristaService
        extends BaseService<ConfiguracionCorreoMayorista, ConfiguracionCorreoMayoristaRequest, ConfiguracionCorreoMayoristaResponse> {

    private final ConfiguracionCorreoMayoristaRepository configuracionCorreoMayoristaRepository;
    private final ConfiguracionCorreoMayoristaMapper configuracionCorreoMayoristaMapper;
    private final MayoristaService mayoristaService;

    public ConfiguracionCorreoMayoristaService(
            ConfiguracionCorreoMayoristaRepository configuracionCorreoMayoristaRepository,
            ConfiguracionCorreoMayoristaMapper configuracionCorreoMayoristaMapper,
            MayoristaService mayoristaService) {
        super(configuracionCorreoMayoristaRepository, configuracionCorreoMayoristaMapper, "Configuración de Correo Mayorista");
        this.configuracionCorreoMayoristaRepository = configuracionCorreoMayoristaRepository;
        this.configuracionCorreoMayoristaMapper = configuracionCorreoMayoristaMapper;
        this.mayoristaService = mayoristaService;
    }

    @Override
    protected ConfiguracionCorreoMayoristaRequest prepararRequest(ConfiguracionCorreoMayoristaRequest request) {
        if (request == null) {
            return null;
        }
        return new ConfiguracionCorreoMayoristaRequest(
                request.correo() != null ? request.correo().trim() : null,
                request.clave(),
                request.puerto() != null ? request.puerto().trim() : null,
                request.smtp() != null ? request.smtp().trim() : null,
                request.tls()
        );
    }

    @Transactional(readOnly = true)
    public ConfiguracionCorreoMayoristaResponse buscarPorMayorista(String mayoristaId) {
        mayoristaService.obtenerEntidad(mayoristaId);
        ConfiguracionCorreoMayorista config = configuracionCorreoMayoristaRepository
                .findByMayoristaIdAndEliminadoFalse(mayoristaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró configuración de correo para el mayorista con id: " + mayoristaId));
        return mapper.aResponse(config);
    }

    @Transactional
    public ConfiguracionCorreoMayoristaResponse modificarPorMayorista(
            String mayoristaId, ConfiguracionCorreoMayoristaRequest request) {
        Mayorista mayorista = mayoristaService.obtenerEntidad(mayoristaId);
        ConfiguracionCorreoMayoristaRequest preparado = prepararRequest(request);
        validarFormato(preparado);

        ConfiguracionCorreoMayorista entidad = configuracionCorreoMayoristaRepository
                .findByMayoristaIdAndEliminadoFalse(mayoristaId)
                .orElseGet(() -> {
                    ConfiguracionCorreoMayorista nueva = configuracionCorreoMayoristaMapper.aEntidad(preparado);
                    nueva.setMayorista(mayorista);
                    nueva.setEliminado(false);
                    return nueva;
                });

        if (entidad.getId() != null) {
            configuracionCorreoMayoristaMapper.actualizarEntidad(preparado, entidad);
            entidad.setClave(preparado.clave());
        } else {
            entidad.setClave(preparado.clave());
        }

        ConfiguracionCorreoMayorista guardada = configuracionCorreoMayoristaRepository.save(entidad);
        return mapper.aResponse(guardada);
    }

    private void validarFormato(ConfiguracionCorreoMayoristaRequest request) {
        if (request == null) {
            throw new ValidacionException("Los datos de configuración de correo son obligatorios");
        }
        if (request.correo() == null || request.correo().isBlank()) {
            throw new ValidacionException("El correo es obligatorio");
        }
        if (request.clave() == null || request.clave().isBlank()) {
            throw new ValidacionException("La clave SMTP es obligatoria");
        }
        if (request.puerto() == null || request.puerto().isBlank()) {
            throw new ValidacionException("El puerto es obligatorio");
        }
        if (request.smtp() == null || request.smtp().isBlank()) {
            throw new ValidacionException("El servidor SMTP es obligatorio");
        }
        if (request.tls() == null) {
            throw new ValidacionException("La configuración TLS es obligatoria");
        }
    }
}

