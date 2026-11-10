package com.DulceMayor.backend.repositories.facturacion;

import com.DulceMayor.backend.entities.facturacion.WebhookNotificationLog;
import com.DulceMayor.backend.repositories.BaseRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para la entidad {@link WebhookNotificationLog}.
 */
@Repository
public interface WebhookNotificationLogRepository extends BaseRepository<WebhookNotificationLog> {

    Optional<WebhookNotificationLog> findByIdTransaccionMpAndEstadoProcesamiento(
            String idTransaccionMp, String estadoProcesamiento);

    boolean existsByIdTransaccionMpAndEstadoProcesamiento(String idTransaccionMp, String estadoProcesamiento);
}
