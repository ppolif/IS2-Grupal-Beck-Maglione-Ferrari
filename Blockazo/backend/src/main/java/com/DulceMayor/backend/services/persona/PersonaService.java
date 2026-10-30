package com.DulceMayor.backend.services.persona;

import com.DulceMayor.backend.config.ConflictoException;
import com.DulceMayor.backend.config.RecursoNoEncontradoException;
import com.DulceMayor.backend.dtos.PerfilResponse;
import com.DulceMayor.backend.dtos.UsuarioResponse;
import com.DulceMayor.backend.entities.Usuario;
import com.DulceMayor.backend.entities.persona.Persona;
import com.DulceMayor.backend.repositories.persona.PersonaRepository;
import com.DulceMayor.backend.services.UsuarioService;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de negocio transversal para la jerarquía de {@link Persona}.
 * Valida unicidad de documento de identidad y resuelve el perfil extendido en {@code GET /auth/perfil}.
 */
@Service
public class PersonaService {

    private final PersonaRepository personaRepository;
    private final UsuarioService usuarioService;

    public PersonaService(PersonaRepository personaRepository, UsuarioService usuarioService) {
        this.personaRepository = personaRepository;
        this.usuarioService = usuarioService;
    }

    @Transactional(readOnly = true)
    public void validarDocumentoUnico(String numeroDocumento) {
        if (personaRepository.existsByNumeroDocumentoAndEliminadoFalse(numeroDocumento)) {
            throw new ConflictoException(
                    "DUPLICADO",
                    "Ya existe una persona registrada con el documento " + numeroDocumento
            );
        }
    }

    @Transactional(readOnly = true)
    public void validarDocumentoUnicoExcluyendoId(String numeroDocumento, String id) {
        if (personaRepository.existsByNumeroDocumentoAndIdNotAndEliminadoFalse(numeroDocumento, id)) {
            throw new ConflictoException(
                    "DUPLICADO",
                    "Ya existe otra persona registrada con el documento " + numeroDocumento
            );
        }
    }

    @Transactional(readOnly = true)
    public Optional<Persona> buscarPorUsuarioId(String usuarioId) {
        return personaRepository.findByUsuarioIdAndEliminadoFalse(usuarioId);
    }

    @Transactional(readOnly = true)
    public Persona obtenerEntidad(String id) {
        return personaRepository.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Persona no encontrada con id: " + id));
    }

    @Transactional(readOnly = true)
    public PerfilResponse obtenerPerfilPorUsuario(String usuarioId) {
        Usuario usuario = usuarioService.buscarUsuario(usuarioId);
        UsuarioResponse usuarioResponse = usuarioService.toResponse(usuario);

        Optional<Persona> personaOpt = personaRepository.findByUsuarioIdAndEliminadoFalse(usuarioId);
        if (personaOpt.isPresent()) {
            Persona persona = personaOpt.get();
            return new PerfilResponse(
                    usuarioResponse,
                    persona.getId(),
                    persona.getNombre(),
                    persona.getApellido()
            );
        }

        return new PerfilResponse(usuarioResponse, null, null, null);
    }
}
