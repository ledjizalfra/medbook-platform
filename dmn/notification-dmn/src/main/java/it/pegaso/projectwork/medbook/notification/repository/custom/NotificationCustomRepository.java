package it.pegaso.projectwork.medbook.notification.repository.custom;

import it.pegaso.projectwork.medbook.notification.entity.NotificationEntity;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationChannelEnum;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationStatusEnum;
import it.pegaso.projectwork.medbook.notification.model.enums.NotificationTypeEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

/**
 * Interfaccia per le query custom con filtri opzionali sulla tabella NOTIFICATIONS.
 */
public interface NotificationCustomRepository {

    /** Ricerca paginata con filtri opzionali. null = nessun filtro applicato. */
    Page<NotificationEntity> searchWithFilters(
            String appointmentId,
            String patientId,
            NotificationTypeEnum type,
            NotificationChannelEnum channel,
            NotificationStatusEnum status,
            LocalDate dateFrom,
            LocalDate dateTo,
            Pageable pageable);
}
