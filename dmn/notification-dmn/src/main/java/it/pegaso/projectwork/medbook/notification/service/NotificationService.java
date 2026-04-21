package it.pegaso.projectwork.medbook.notification.service;

import it.pegaso.projectwork.medbook.commons.api.model.MedBookContext;
import it.pegaso.projectwork.medbook.notification.entity.NotificationEntity;
import it.pegaso.projectwork.medbook.notification.model.NotificationTemplateModel;
import it.pegaso.projectwork.medbook.notification.server.model.*;

import java.time.LocalDate;

/**
 * Interfaccia del service per la gestione delle notifiche.
 */
public interface NotificationService {

    /** Processa l'invio di una notifica con retry.
     * Chiamato dal consumer Kafka e dall'endpoint di retry manuale. */
    void process(NotificationEntity notification, NotificationTemplateModel model);

    /** Recupera la lista paginata delle notifiche con filtri opzionali. */
    NotificationListOutput getNotifications(MedBookContext context,
                                             Integer page, Integer size, String sort,
                                             String appointmentId, String patientId,
                                             NotificationTypeApiEnum type,
                                             NotificationChannelApiEnum channel,
                                             NotificationStatusApiEnum status,
                                             LocalDate dateFrom, LocalDate dateTo);

    /** Recupera una singola notifica per business key. */
    NotificationDetailOutput getNotificationById(MedBookContext context, String notificationId);

    /** Reinvia manualmente una notifica in stato FALLITA (solo ROLE_ADMIN). */
    NotificationDetailOutput retryNotification(MedBookContext context, String notificationId);
}
