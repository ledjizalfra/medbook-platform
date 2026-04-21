package it.pegaso.projectwork.medbook.notification.helper;

import it.pegaso.projectwork.medbook.commons.errors.exceptions.MedBookNotFoundException;
import it.pegaso.projectwork.medbook.notification.constants.NotificationConstants;
import it.pegaso.projectwork.medbook.notification.entity.NotificationEntity;
import it.pegaso.projectwork.medbook.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Helper per le operazioni di dominio comuni alle notifiche.
 * Centralizza la generazione della business key e il recupero dell'entita.
 */
@Component
@RequiredArgsConstructor
public class NotificationDomainHelper {

    private final NotificationRepository notificationRepository;

    /** Genera la business key nel formato NOT-{seq} usando la sequenza PostgreSQL. */
    public String generateNotificationId() {
        Long seq = notificationRepository.getNextNotificationSequenceValue();
        return NotificationConstants.NOT_PREFIX + seq;
    }

    /** Recupera una notifica per business key o lancia MedBookNotFoundException. */
    public NotificationEntity retrieveOrThrow(String notificationId) {
        return notificationRepository.findByNotificationId(notificationId)
                .orElseThrow(() -> new MedBookNotFoundException(
                        "notifica non trovata: " + notificationId));
    }
}
