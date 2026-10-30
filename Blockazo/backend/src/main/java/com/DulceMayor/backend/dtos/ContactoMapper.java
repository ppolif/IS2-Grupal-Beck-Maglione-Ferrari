package com.DulceMayor.backend.dtos;

import com.DulceMayor.backend.config.ValidacionException;
import com.DulceMayor.backend.entities.contacto.Contacto;
import com.DulceMayor.backend.entities.contacto.ContactoCorreoElectronico;
import com.DulceMayor.backend.entities.contacto.ContactoTelefonico;
import java.util.regex.Pattern;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper polimórfico de MapStruct para convertir entre la jerarquía de entidades {@link Contacto}
 * ({@link ContactoCorreoElectronico} y {@link ContactoTelefonico}) y sus DTOs planos
 * ({@link ContactoRequest} y {@link ContactoResponse}).
 *
 * <p><b>Patrones aplicados: DTO + Adapter + Factory Method polimórfico.</b></p>
 * <p><b>Por qué se aplica acá:</b> El contrato OpenAPI define una estructura plana con el
 * discriminador {@code medio} ({@code "CORREO"} o {@code "TELEFONICO"}), mientras que el modelo
 * de dominio modela una jerarquía de clases persistida con JPA {@code SINGLE_TABLE}. Este mapper
 * actúa como adaptador y fábrica que instancia la subclase concreta correspondiente al convertir
 * hacia el dominio y aplana las propiedades específicas al proyectar hacia la respuesta.</p>
 */
@Mapper(componentModel = "spring")
public interface ContactoMapper extends BaseMapper<Contacto, ContactoRequest, ContactoResponse> {

    Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    ContactoCorreoElectronico aEntidadCorreo(ContactoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    ContactoTelefonico aEntidadTelefonico(ContactoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    void actualizarEntidadCorreo(ContactoRequest request, @MappingTarget ContactoCorreoElectronico entidad);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eliminado", ignore = true)
    void actualizarEntidadTelefonico(ContactoRequest request, @MappingTarget ContactoTelefonico entidad);

    @Mapping(target = "medio", constant = Contacto.MEDIO_CORREO)
    @Mapping(target = "telefono", ignore = true)
    @Mapping(target = "tipoTelefono", ignore = true)
    ContactoResponse aResponseCorreo(ContactoCorreoElectronico entidad);

    @Mapping(target = "medio", constant = Contacto.MEDIO_TELEFONICO)
    @Mapping(target = "email", ignore = true)
    ContactoResponse aResponseTelefonico(ContactoTelefonico entidad);

    @Override
    default Contacto aEntidad(ContactoRequest request) {
        ContactoRequest normalizado = normalizarYValidar(request);
        if (Contacto.MEDIO_CORREO.equals(normalizado.medio())) {
            return aEntidadCorreo(normalizado);
        }
        return aEntidadTelefonico(normalizado);
    }

    @Override
    default void actualizarEntidad(ContactoRequest request, @MappingTarget Contacto entidad) {
        if (entidad == null) {
            throw new ValidacionException("La entidad de contacto a actualizar no puede ser nula");
        }
        ContactoRequest normalizado = normalizarYValidar(request);
        if (entidad instanceof ContactoCorreoElectronico correo) {
            if (!Contacto.MEDIO_CORREO.equals(normalizado.medio())) {
                throw new ValidacionException(
                        "No se puede cambiar el medio de un contacto de CORREO a " + normalizado.medio()
                );
            }
            actualizarEntidadCorreo(normalizado, correo);
        } else if (entidad instanceof ContactoTelefonico telefonico) {
            if (!Contacto.MEDIO_TELEFONICO.equals(normalizado.medio())) {
                throw new ValidacionException(
                        "No se puede cambiar el medio de un contacto TELEFONICO a " + normalizado.medio()
                );
            }
            actualizarEntidadTelefonico(normalizado, telefonico);
        } else {
            throw new ValidacionException(
                    "Subclase de Contacto no soportada: " + entidad.getClass().getSimpleName()
            );
        }
    }

    @Override
    default ContactoResponse aResponse(Contacto entidad) {
        if (entidad == null) {
            return null;
        }
        if (entidad instanceof ContactoCorreoElectronico correo) {
            return aResponseCorreo(correo);
        }
        if (entidad instanceof ContactoTelefonico telefonico) {
            return aResponseTelefonico(telefonico);
        }
        throw new IllegalArgumentException(
                "Subclase de Contacto no soportada: " + entidad.getClass().getSimpleName()
        );
    }

    /**
     * Normaliza cadenas (trim) y valida las invariantes de negocio polimórficas según el campo {@code medio}.
     */
    default ContactoRequest normalizarYValidar(ContactoRequest request) {
        if (request == null) {
            throw new ValidacionException("Los datos del contacto son obligatorios");
        }
        if (request.medio() == null || request.medio().isBlank()) {
            throw new ValidacionException("El medio de contacto es obligatorio (CORREO o TELEFONICO)");
        }
        if (request.tipoContacto() == null) {
            throw new ValidacionException("El tipo de contacto es obligatorio");
        }

        String medioNormalizado = request.medio().trim().toUpperCase();
        String observacionNormalizada = request.observacion() != null && !request.observacion().isBlank()
                ? request.observacion().trim()
                : null;
        if (observacionNormalizada != null && observacionNormalizada.length() > 255) {
            throw new ValidacionException("La observación del contacto no puede superar los 255 caracteres");
        }

        if (Contacto.MEDIO_CORREO.equals(medioNormalizado)) {
            if (request.email() == null || request.email().isBlank()) {
                throw new ValidacionException("El email es obligatorio cuando el medio de contacto es CORREO");
            }
            String emailNormalizado = request.email().trim();
            if (emailNormalizado.length() > 150) {
                throw new ValidacionException("El correo electrónico no puede superar los 150 caracteres");
            }
            if (!EMAIL_PATTERN.matcher(emailNormalizado).matches()) {
                throw new ValidacionException("El formato del correo electrónico no es válido");
            }
            return new ContactoRequest(
                    medioNormalizado,
                    request.tipoContacto(),
                    observacionNormalizada,
                    emailNormalizado,
                    null,
                    null
            );
        }

        if (Contacto.MEDIO_TELEFONICO.equals(medioNormalizado)) {
            if (request.telefono() == null || request.telefono().isBlank()) {
                throw new ValidacionException("El teléfono es obligatorio cuando el medio de contacto es TELEFONICO");
            }
            if (request.tipoTelefono() == null) {
                throw new ValidacionException("El tipo de teléfono es obligatorio cuando el medio de contacto es TELEFONICO");
            }
            String telefonoNormalizado = request.telefono().trim();
            if (telefonoNormalizado.length() > 30) {
                throw new ValidacionException("El teléfono no puede superar los 30 caracteres");
            }
            return new ContactoRequest(
                    medioNormalizado,
                    request.tipoContacto(),
                    observacionNormalizada,
                    null,
                    telefonoNormalizado,
                    request.tipoTelefono()
            );
        }

        throw new ValidacionException(
                "El medio de contacto '" + request.medio() + "' no es válido. Valores permitidos: CORREO, TELEFONICO"
        );
    }
}

