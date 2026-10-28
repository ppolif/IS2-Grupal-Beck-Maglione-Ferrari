package com.DulceMayor.backend.repositories;

import com.DulceMayor.backend.entities.contacto.Contacto;
import com.DulceMayor.backend.entities.contacto.ContactoCorreoElectronico;
import com.DulceMayor.backend.entities.contacto.ContactoTelefonico;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repositorio polimórfico de acceso a datos para la jerarquía {@link Contacto}.
 * Permite consultar tanto cualquier {@link Contacto} como filtrar por sus subclases
 * {@link ContactoCorreoElectronico} y {@link ContactoTelefonico}.
 */
@Repository
public interface ContactoRepository extends BaseRepository<Contacto> {

    @Query("SELECT c FROM ContactoCorreoElectronico c WHERE c.eliminado = false")
    List<ContactoCorreoElectronico> findAllCorreosActivos();

    @Query("SELECT c FROM ContactoTelefonico c WHERE c.eliminado = false")
    List<ContactoTelefonico> findAllTelefonosActivos();
}

