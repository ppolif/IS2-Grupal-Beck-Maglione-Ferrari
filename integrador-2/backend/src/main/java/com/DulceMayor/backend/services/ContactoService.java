package com.DulceMayor.backend.services;

import com.DulceMayor.backend.dtos.BaseMapper;
import com.DulceMayor.backend.dtos.ContactoMapper;
import com.DulceMayor.backend.dtos.ContactoRequest;
import com.DulceMayor.backend.dtos.ContactoResponse;
import com.DulceMayor.backend.entities.contacto.Contacto;
import com.DulceMayor.backend.repositories.BaseRepository;
import com.DulceMayor.backend.repositories.ContactoRepository;
import org.springframework.stereotype.Service;

/**
 * Servicio interno de dominio para la gestión polimórfica de {@link Contacto}
 * ({@code ContactoCorreoElectronico} y {@code ContactoTelefonico}).
 *
 * <p>No expone un controlador REST propio ya que los contactos se administran en cascada o por
 * composición desde {@code Persona} ({@code Empleado}, {@code Minorista}) y {@code Mayorista}.
 * Extiende {@link BaseService} siguiendo el patrón Template Method para proveer validación,
 * conversión polimórfica y acceso interno ({@link #obtenerEntidad(String)}) sin romper la regla
 * de arquitectura que prohíbe a otros servicios inyectar {@link ContactoRepository} directamente.</p>
 */
@Service
public class ContactoService extends BaseService<Contacto, ContactoRequest, ContactoResponse> {

    private final ContactoMapper contactoMapper;

    public ContactoService(
            ContactoRepository contactoRepository,
            ContactoMapper contactoMapper) {
        super(contactoRepository, contactoMapper, "Contacto");
        this.contactoMapper = contactoMapper;
    }

    @Override
    protected ContactoRequest prepararRequest(ContactoRequest request) {
        return contactoMapper.normalizarYValidar(request);
    }

    @Override
    protected void validarCrear(ContactoRequest request) {
        contactoMapper.normalizarYValidar(request);
    }

    @Override
    protected void validarModificar(String id, ContactoRequest request, Contacto existente) {
        contactoMapper.normalizarYValidar(request);
    }
}

