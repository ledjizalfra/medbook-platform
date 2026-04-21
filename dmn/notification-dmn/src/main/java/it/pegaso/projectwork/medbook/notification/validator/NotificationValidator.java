package it.pegaso.projectwork.medbook.notification.validator;

import it.pegaso.projectwork.medbook.notification.entity.NotificationEntity;

/**
 * Interfaccia del validator di dominio per le notifiche.
 */
public interface NotificationValidator {

    /** Verifica che la notifica sia in stato FALLITA prima del retry manuale.
     * Lancia MedBookBusinessException se lo stato non è FALLITA. */
    void validateRetryNotification(NotificationEntity notification);
}
