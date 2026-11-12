package com.DulceMayor.backend.dtos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.DulceMayor.backend.config.ValidacionException;
import com.DulceMayor.backend.entities.contacto.Contacto;
import com.DulceMayor.backend.entities.contacto.ContactoCorreoElectronico;
import com.DulceMayor.backend.entities.contacto.ContactoTelefonico;
import com.DulceMayor.backend.entities.contacto.TipoContacto;
import com.DulceMayor.backend.entities.contacto.TipoTelefono;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class ContactoMapperTest {

    private ContactoMapper contactoMapper;
    private Validator validator;

    @BeforeEach
    void setUp() {
        contactoMapper = Mappers.getMapper(ContactoMapper.class);
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("ContactoRequest bean validation acepta CORREO, TELEFONICO y TELEFONO")
    void contactoRequestBeanValidationPermiteTelefonia() {
        ContactoRequest reqCorreo = new ContactoRequest("CORREO", TipoContacto.LABORAL, null, "user@test.com", null, null);
        ContactoRequest reqTelefonico = new ContactoRequest("TELEFONICO", TipoContacto.LABORAL, null, null, "2615551234", TipoTelefono.CELULAR);
        ContactoRequest reqTelefono = new ContactoRequest("TELEFONO", TipoContacto.LABORAL, null, null, "2615551234", null);

        assertTrue(validator.validate(reqCorreo).isEmpty());
        assertTrue(validator.validate(reqTelefonico).isEmpty());
        assertTrue(validator.validate(reqTelefono).isEmpty());

        ContactoRequest reqInvalido = new ContactoRequest("INVALIDO", TipoContacto.LABORAL, null, null, "123", null);
        assertEquals(1, validator.validate(reqInvalido).size());
    }

    @Test
    @DisplayName("normalizarYValidar normaliza TELEFONO a TELEFONICO y asigna CELULAR por defecto si tipoTelefono es nulo")
    void normalizarYValidarSoportaTelefono() {
        ContactoRequest req = new ContactoRequest("TELEFONO", TipoContacto.LABORAL, null, null, "  2615551234  ", null);
        ContactoRequest normalizado = contactoMapper.normalizarYValidar(req);

        assertEquals("TELEFONICO", normalizado.medio());
        assertEquals("2615551234", normalizado.telefono());
        assertEquals(TipoTelefono.CELULAR, normalizado.tipoTelefono());

        Contacto entidad = contactoMapper.aEntidad(normalizado);
        assertInstanceOf(ContactoTelefonico.class, entidad);
        ContactoTelefonico telEntidad = (ContactoTelefonico) entidad;
        assertEquals("2615551234", telEntidad.getTelefono());
        assertEquals(TipoTelefono.CELULAR, telEntidad.getTipoTelefono());
    }

    @Test
    @DisplayName("normalizarYValidar procesa CORREO correctamente")
    void normalizarYValidarCorreo() {
        ContactoRequest req = new ContactoRequest("CORREO", TipoContacto.LABORAL, null, "  empleado@empresa.com  ", null, null);
        ContactoRequest normalizado = contactoMapper.normalizarYValidar(req);

        assertEquals("CORREO", normalizado.medio());
        assertEquals("empleado@empresa.com", normalizado.email());

        Contacto entidad = contactoMapper.aEntidad(normalizado);
        assertInstanceOf(ContactoCorreoElectronico.class, entidad);
        ContactoCorreoElectronico mailEntidad = (ContactoCorreoElectronico) entidad;
        assertEquals("empleado@empresa.com", mailEntidad.getEmail());
    }

    @Test
    @DisplayName("normalizarYValidar rechaza medio inválido")
    void rechazaMedioInvalido() {
        ContactoRequest req = new ContactoRequest("OTRO", TipoContacto.LABORAL, null, null, "1234", null);
        assertThrows(ValidacionException.class, () -> contactoMapper.normalizarYValidar(req));
    }
}
